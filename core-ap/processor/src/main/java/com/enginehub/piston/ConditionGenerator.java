package com.enginehub.piston;

import com.enginehub.piston.annotation.CommandConditionGenerator;
import com.enginehub.piston.annotation.DependencySupport;
import com.squareup.javapoet.CodeBlock;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;
import java.lang.reflect.InvocationTargetException;

import static com.google.auto.common.AnnotationMirrors.getAnnotationValue;
import static com.google.auto.common.MoreTypes.asTypeElement;

class ConditionGenerator {
    private final AnnotationMirror conditionMirror;
    private final ExecutableElement method;
    private final DependencySupport depSupport;

    ConditionGenerator(AnnotationMirror conditionMirror, ExecutableElement method, DependencySupport depSupport) {
        this.conditionMirror = conditionMirror;
        this.method = method;
        this.depSupport = depSupport;
    }

    CodeBlock generateCondition() {
        String generatorClassName = getAnnotationValue(conditionMirror, "type")
            .accept(new SimpleAnnotationValueVisitor8<String, Void>() {
                @Override
                public String visitType(TypeMirror t, Void aVoid) {
                    return asTypeElement(t).getQualifiedName().toString();
                }
            }, null);
        if (generatorClassName == null) {
            throw new ProcessingException("No generator class name.")
                .withElement(method).withAnnotation(conditionMirror);
        }
        Class<?> uncastGeneratorClass;
        try {
            uncastGeneratorClass = Class.forName(generatorClassName);
        } catch (ClassNotFoundException e) {
            throw new ProcessingException("Class " + generatorClassName + " not found", e)
                .withElement(method).withAnnotation(conditionMirror);
        }

        Class<? extends CommandConditionGenerator> generatorClass;
        try {
            generatorClass = uncastGeneratorClass
                .asSubclass(CommandConditionGenerator.class);
        } catch (ClassCastException e) {
            throw new ProcessingException("Class " + generatorClassName + " is not a CCG", e)
                .withElement(method).withAnnotation(conditionMirror);
        }

        CommandConditionGenerator generator;
        try {
            generator = generatorClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException
            | InvocationTargetException | NoSuchMethodException e) {
            // too lazy to sort these, some of them shouldn't happen / aren't easy to cause
            throw new ProcessingException("Problem with default constructor of " + generatorClassName, e)
                .withElement(method).withAnnotation(conditionMirror);
        }
        return generator.generateCondition(conditionMirror, method, depSupport);
    }
}
