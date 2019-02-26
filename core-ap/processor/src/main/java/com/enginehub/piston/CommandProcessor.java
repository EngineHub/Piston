package com.enginehub.piston;

import com.enginehub.piston.annotation.Command;
import com.enginehub.piston.annotation.CommandCondition;
import com.enginehub.piston.annotation.CommandContainer;
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

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.auto.common.AnnotationMirrors.getAnnotationValue;
import static com.google.auto.common.MoreElements.asType;
import static com.google.auto.common.MoreElements.getAnnotationMirror;
import static com.google.auto.common.MoreElements.getPackage;
import static com.google.auto.common.MoreElements.isAnnotationPresent;
import static com.google.common.collect.ImmutableList.toImmutableList;

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
        for (Element element : elements) {
            TypeElement type = asType(element);
            ImmutableList<CommandInfo> info = type.getEnclosedElements().stream()
                .filter(enclosedElement -> enclosedElement.getKind() == ElementKind.METHOD)
                .filter(m -> isAnnotationPresent(m, Command.class))
                .map(MoreElements::asExecutable)
                .map(this::getCommandInfo)
                .collect(toImmutableList());
            try {
                new CommandRegistrationGenerator(type.getSimpleName() + "Registration", info)
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

    private CommandInfo getCommandInfo(ExecutableElement method) {
        AnnotationMirror mirror = getAnnotationMirror(method, Command.class)
            .toJavaUtil().orElseThrow(() -> new IllegalStateException("Should have a value"));

        Optional<AnnotationMirror> conditionMirror = findCommandCondition(method);
        CommandInfo.Builder builder = CommandInfo.builder();
        builder.condition(conditionMirror.map(m ->
            new ConditionGenerator(m, method, new CommandInfoDependencySupport(builder))
                .generateCondition()
        ).orElse(null));

        String name = (String) getAnnotationValue(mirror, "name").getValue();
        @SuppressWarnings("unchecked")
        List<String> aliasesList = (List<String>) getAnnotationValue(mirror, "aliases").getValue();
        ImmutableList<String> aliases = ImmutableList.copyOf(
            aliasesList
        );
        String desc = (String) getAnnotationValue(mirror, "desc").getValue();
        String descFooter = (String) getAnnotationValue(mirror, "descFooter").getValue();
        if (descFooter.equals("")) {
            descFooter = null;
        }
        List<CommandPartInfo> parts = new CommandParameterInterpreter(method)
            .getParts();
        return builder
            .name(name)
            .aliases(aliases)
            .description(desc)
            .footer(descFooter)
            .parts(parts)
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
