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

package org.enginehub.piston.gen;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import org.enginehub.piston.Command;
import org.enginehub.piston.gen.util.ProcessingException;
import org.enginehub.piston.gen.util.SafeName;
import org.enginehub.piston.gen.value.CommandCondInfo;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;
import java.lang.reflect.Method;

import static com.google.auto.common.AnnotationMirrors.getAnnotationValue;
import static org.enginehub.piston.gen.value.ReservedNames.GET_COMMAND_METHOD;

class ConditionGenerator {
    private final AnnotationMirror conditionMirror;
    private final ExecutableElement method;
    private final GenerationSupport depSupport;

    ConditionGenerator(AnnotationMirror conditionMirror,
                       ExecutableElement method,
                       GenerationSupport depSupport) {
        this.conditionMirror = conditionMirror;
        this.method = method;
        this.depSupport = depSupport;
    }

    CommandCondInfo generateCondition() {
        TypeName generatorClassName = getAnnotationValue(conditionMirror, "value")
            .accept(new SimpleAnnotationValueVisitor8<TypeName, Void>() {
                @Override
                public TypeName visitType(TypeMirror t, Void aVoid) {
                    return TypeName.get(t);
                }
            }, null);
        if (generatorClassName == null) {
            throw new ProcessingException("No generator class name.")
                .withElement(method).withAnnotation(conditionMirror);
        }
        // Request an instance of the class. It can be shared if it's the same class.
        String condGeneratorFieldName = depSupport.requestDependency(
            generatorClassName, SafeName.getNameAsIdentifier(generatorClassName),
            generatorClassName
        );
        String commandMethodField = "commandMethod";
        String conditionField = "condition";
        // Do the actual setup & store:
        return CommandCondInfo.builder()
            .condVariable(conditionField)
            .construction(CodeBlock.builder()
                .addStatement(
                    "$T $L = $L($S)",
                    Method.class, commandMethodField,
                    GET_COMMAND_METHOD, method.getSimpleName().toString())
                .addStatement(
                    "$T $L = $L.generateCondition($L)",
                    Command.Condition.class, conditionField,
                    condGeneratorFieldName, commandMethodField
                )
                .build())
            .build();
    }

}
