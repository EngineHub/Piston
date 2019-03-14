/*
 * Piston, a flexible command management system.
 * Copyright (C) EngineHub <http://www.enginehub.com>
 * Copyright (C) Piston contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.piston;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.squareup.javapoet.ClassName;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandCondition;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.GenerationSupport;
import org.enginehub.piston.util.ProcessingException;

import javax.annotation.Nullable;
import javax.annotation.processing.Processor;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.auto.common.MoreElements.asType;
import static com.google.auto.common.MoreElements.getAnnotationMirror;
import static com.google.auto.common.MoreElements.getPackage;
import static com.google.auto.common.MoreElements.isAnnotationPresent;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.enginehub.piston.util.AnnoValueExtraction.getList;
import static org.enginehub.piston.util.AnnoValueExtraction.getValue;

@AutoService(Processor.class)
public class CommandProcessor extends BasicAnnotationProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    protected Iterable<? extends ProcessingStep> initSteps() {
        return ImmutableList.of(new ProcessingStep() {
            @Override
            public Set<? extends Class<? extends Annotation>> annotations() {
                return ImmutableSet.of(CommandContainer.class);
            }

            @Override
            public Set<Element> process(SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
                try {
                    return doProcess(elementsByAnnotation.get(CommandContainer.class));
                } catch (ProcessingException e) {
                    StringBuilder message = new StringBuilder(e.getMessage());
                    if (e.getCause() != null) {
                        message.append('\n')
                            .append(Throwables.getStackTraceAsString(e.getCause()));
                    }
                    processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        message,
                        e.getCausingElement(),
                        e.getCausingMirror()
                    );
                    return ImmutableSet.of();
                }
            }
        });
    }

    private Set<Element> doProcess(Set<Element> elements) {
        ClassName javaxInjectClassName = findJavaxInject();
        for (Element element : elements) {
            TypeElement type = asType(element);
            ImmutableList<CommandInfo> info = type.getEnclosedElements().stream()
                .filter(enclosedElement -> enclosedElement.getKind() == ElementKind.METHOD)
                .filter(m -> isAnnotationPresent(m, org.enginehub.piston.annotation.Command.class))
                .map(MoreElements::asExecutable)
                .map(this::getCommandInfo)
                .collect(toImmutableList());
            try {
                new CommandRegistrationGenerator(
                    RegistrationInfo.builder()
                        .name(type.getSimpleName() + "Registration")
                        .targetClassName(ClassName.get(type))
                        .classVisibility(visibility(type.getModifiers()))
                        .commands(info)
                        .javaxInjectClassName(javaxInjectClassName)
                        .build())
                    .generate(element,
                        getPackage(element).getQualifiedName().toString(),
                        processingEnv.getFiler());
            } catch (IOException e) {
                throw new ProcessingException("Error writing generated file", e)
                    .withElement(element);
            }
        }
        return ImmutableSet.of();
    }

    private static final ImmutableSet<Modifier> VISIBILITY_MODIFIERS = Sets.immutableEnumSet(
        Modifier.PUBLIC,
        Modifier.PROTECTED,
        Modifier.PRIVATE
    );

    @Nullable
    private Modifier visibility(Set<Modifier> modifiers) {
        return modifiers.stream()
            .filter(VISIBILITY_MODIFIERS::contains)
            .findAny().orElse(null);
    }

    @Nullable
    private ClassName findJavaxInject() {
        return Optional.ofNullable(processingEnv.getElementUtils().getTypeElement("javax.inject.Inject"))
            .map(ClassName::get)
            .orElse(null);
    }

    private CommandInfo getCommandInfo(ExecutableElement method) {
        AnnotationMirror mirror = getAnnotationMirror(method, Command.class)
            .toJavaUtil().orElseThrow(() -> new IllegalStateException("Should have a value"));

        Optional<AnnotationMirror> conditionMirror = findCommandCondition(method);
        CommandInfo.Builder builder = CommandInfo.builder();
        GenerationSupport generationSupport = new CommandInfoGenerationSupport(builder);
        builder.condition(conditionMirror.map(m ->
            new ConditionGenerator(m, method, generationSupport)
                .generateCondition()
        ).orElse(null));

        String name = getValue(method, mirror, "name", String.class);
        @SuppressWarnings("unchecked")
        List<String> aliasesList = getList(method, mirror, "aliases", String.class);
        ImmutableList<String> aliases = ImmutableList.copyOf(
            aliasesList
        );
        String desc = getValue(method, mirror, "desc", String.class);
        String descFooter = getValue(method, mirror, "descFooter", String.class);
        if (descFooter.equals("")) {
            descFooter = null;
        }
        List<CommandParamInfo> params = new CommandParameterInterpreter(method, generationSupport)
            .getParams();
        return builder
            .commandMethod(method)
            .name(name)
            .aliases(aliases)
            .description(desc)
            .footer(descFooter)
            .params(params)
            .build();
    }

    private final LoadingCache<TypeElement, Boolean> IS_CONDITION =
        CacheBuilder.newBuilder()
            .weakKeys()
            // take 50 falses, or many more trues
            // we value a positive result more
            .maximumWeight(5000)
            .<TypeElement, Boolean>weigher((k, v) -> v ? 1 : 100)
            .build(CacheLoader.from((TypeElement element) -> {
                if (element == null) {
                    return false;
                }
                if (element.getQualifiedName().contentEquals(CommandCondition.class.getCanonicalName())) {
                    return true;
                }
                return isAnnotationPresent(element, CommandCondition.class);
            }));

    private Optional<AnnotationMirror> findCommandCondition(ExecutableElement method) {
        return method.getAnnotationMirrors().stream()
            .filter(mirror -> IS_CONDITION.getUnchecked(
                asType(mirror.getAnnotationType().asElement())
            ))
            // reset the generic to just AnnotationMirror, no wildcard
            .map(x -> (AnnotationMirror) x)
            .map(Optional::of)
            .findFirst()
            .orElse(Optional.empty());
    }

}