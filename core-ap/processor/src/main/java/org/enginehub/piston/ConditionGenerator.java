/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) EngineHub <http://www.enginehub.com>
 * Copyright (C) oblique-commands contributors
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

import org.enginehub.piston.annotation.CommandConditionGenerator;
import org.enginehub.piston.annotation.DependencySupport;
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
