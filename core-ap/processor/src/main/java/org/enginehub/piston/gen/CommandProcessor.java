/*
 * Piston, a flexible command management system.
 * Copyright (C) EngineHub <https://www.enginehub.org>
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

package org.enginehub.piston.gen;

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
import org.enginehub.piston.gen.optimize.CommandInfoOptimization;
import org.enginehub.piston.gen.optimize.CommandParamInfoOptimization;
import org.enginehub.piston.gen.optimize.ExtractSpecOptimization;
import org.enginehub.piston.gen.util.ProcessingException;
import org.enginehub.piston.gen.value.CommandInfo;
import org.enginehub.piston.gen.value.CommandParamInfo;
import org.enginehub.piston.gen.value.RegistrationInfo;

import javax.annotation.Nullable;
import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
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
import static com.google.auto.common.MoreTypes.asTypeElement;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.joining;
import static org.enginehub.piston.gen.util.AnnoValueExtraction.getList;
import static org.enginehub.piston.gen.util.AnnoValueExtraction.getValue;
import static org.enginehub.piston.gen.util.ProcessingEnvValues.ARG_NAME_KEY_PREFIX;

@AutoService(Processor.class)
@SupportedOptions(ARG_NAME_KEY_PREFIX)
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
        for (Element element : elements) {
            try {
                TypeElement type = asType(element);

                RegistrationInfo.Builder registration = RegistrationInfo.builder();
                IdentifierTracker identifierTracker = new IdentifierTracker();
                GenerationSupport generationSupport = new GenerationSupportImpl(
                    identifierTracker, registration
                );

                ImmutableList<CommandInfo> info = type.getEnclosedElements().stream()
                    .filter(enclosedElement -> enclosedElement.getKind() == ElementKind.METHOD)
                    .filter(m -> isAnnotationPresent(m, org.enginehub.piston.annotation.Command.class))
                    .map(MoreElements::asExecutable)
                    .map(m -> getCommandInfo(m, generationSupport))
                    .collect(toImmutableList());
                CommandInfoOptimization rootOptimization = buildOptimizer(identifierTracker);
                info = ImmutableList.copyOf(rootOptimization.optimize(info));

                AnnotationMirror container = getAnnotationMirror(element, CommandContainer.class)
                    .toJavaUtil().orElseThrow(() ->
                        new ProcessingException("Missing CommandContainer annotation")
                            .withElement(element));
                getList(
                    element, container, "superTypes", TypeMirror.class
                ).forEach(t -> registration.addSuperType(asTypeElement(t)));

                try {
                    ClassName className = ClassName.get(type);
                    new CommandRegistrationGenerator(
                        registration
                            .name(getRegistrationClassName(className))
                            .targetClassName(className)
                            .classVisibility(visibility(type.getModifiers()))
                            .commands(info)
                            .build())
                        .generate(element,
                            getPackage(element).getQualifiedName().toString(),
                            processingEnv.getFiler());
                } catch (IOException e) {
                    throw new ProcessingException("Error writing generated file", e)
                        .withElement(element);
                }
            } catch (ProcessingException e) {
                throw e;
            } catch (Exception e) {
                throw new ProcessingException("Error generating code", e)
                    .withElement(element);
            }
        }
        return ImmutableSet.of();
    }

    private String getRegistrationClassName(ClassName className) {
        return className.simpleNames().stream().collect(joining("_", "", "Registration"));
    }

    private CommandInfoOptimization buildOptimizer(IdentifierTracker identifierTracker) {
        return new CommandInfoOptimization(
            new CommandParamInfoOptimization(
                new ExtractSpecOptimization(identifierTracker),
                identifierTracker),
            identifierTracker);
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

    private CommandInfo getCommandInfo(ExecutableElement method, GenerationSupport generationSupport) {
        AnnotationMirror mirror = getAnnotationMirror(method, Command.class)
            .toJavaUtil().orElseThrow(() -> new IllegalStateException("Should have a value"));

        Optional<AnnotationMirror> conditionMirror = findCommandCondition(method);
        CommandInfo.Builder builder = CommandInfo.builder();
        builder.condition(conditionMirror.map(m ->
            new ConditionGenerator(m, method, generationSupport)
                .generateCondition()
        ).orElse(null));

        String name = getValue(method, mirror, "name", String.class);
        List<String> aliasesList = getList(method, mirror, "aliases", String.class);
        ImmutableList<String> aliases = ImmutableList.copyOf(
            aliasesList
        );
        String desc = getValue(method, mirror, "desc", String.class);
        String descFooter = getValue(method, mirror, "descFooter", String.class);
        if (descFooter.equals("")) {
            descFooter = null;
        }
        List<CommandParamInfo> params = new CommandParameterInterpreter(method, generationSupport, processingEnv)
            .getParams();
        return builder
            .commandMethod(method)
            .name(name)
            .generatedName("cmd$" + name)
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
                if (isExactlyConditionAnno(element)) {
                    return true;
                }
                return isAnnotationPresent(element, CommandCondition.class);
            }));

    private static boolean isExactlyConditionAnno(TypeElement element) {
        return element.getQualifiedName().contentEquals(CommandCondition.class.getCanonicalName());
    }

    private Optional<AnnotationMirror> findCommandCondition(ExecutableElement method) {
        return method.getAnnotationMirrors().stream()
            .filter(mirror -> IS_CONDITION.getUnchecked(
                asType(mirror.getAnnotationType().asElement())
            ))
            // reset the generic to just AnnotationMirror, no wildcard
            .map(x -> (AnnotationMirror) x)
            .map(x -> {
                Element annoElement = x.getAnnotationType().asElement();
                return isExactlyConditionAnno(asType(annoElement))
                    ? x
                    : getAnnotationMirror(annoElement, CommandCondition.class).toJavaUtil()
                    .orElseThrow(() -> new ProcessingException("No CommandCondition?")
                        .withElement(method).withAnnotation(x));
            })
            .map(Optional::of)
            .findFirst()
            .orElse(Optional.empty());
    }

}
