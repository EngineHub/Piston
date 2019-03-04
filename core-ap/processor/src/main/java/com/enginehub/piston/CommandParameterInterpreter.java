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

package com.enginehub.piston;

import com.enginehub.piston.annotation.DependencySupport;
import com.enginehub.piston.annotation.param.Arg;
import com.enginehub.piston.annotation.param.Desc;
import com.enginehub.piston.part.CommandArgument;
import com.google.auto.common.MoreElements;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Key;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import static com.enginehub.piston.ReservedVariables.PARAMETERS;
import static com.google.auto.common.AnnotationMirrors.getAnnotationValue;
import static com.google.auto.common.MoreElements.isAnnotationPresent;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;

class CommandParameterInterpreter {

    private interface ParamTransform {

        CommandParamInfo createPartInfo(VariableElement parameter);

    }

    private final Map<Class<? extends Annotation>, ParamTransform> ANNOTATION_TRANSFORMS = ImmutableMap.of(
        Arg.class, this::argTransform
    );
    private final ExecutableElement method;
    private final DependencySupport dependencySupport;

    CommandParameterInterpreter(ExecutableElement method, DependencySupport dependencySupport) {
        this.method = method;
        this.dependencySupport = dependencySupport;
    }

    private MethodSpec.Builder extractSpec(VariableElement param, String name) {
        return MethodSpec.methodBuilder(dependencySupport.requestMethodName(name + "Extract"))
            .addModifiers(Modifier.PRIVATE)
            .addParameter(CommandParameters.class, PARAMETERS)
            .returns(TypeName.get(param.asType()));
    }

    private CommandParamInfo argTransform(VariableElement parameter) {
        Optional<AnnotationMirror> argMirror = MoreElements.getAnnotationMirror(parameter, Arg.class);
        assert argMirror.isPresent();
        AnnotationMirror arg = argMirror.get();
        String name = (String) getAnnotationValue(arg, "value")
            .getValue();
        String desc = getDesc(parameter);
        String var = dependencySupport.requestInScope(
            ClassName.get(CommandArgument.class),
            name + "Part"
        );
        return CommandParamInfo.builder()
            .partVariable(var)
            .construction(CodeBlock.builder()
                .addStatement("$L = $T.builder($S, $S)",
                    var,
                    CommandArgument.class,
                    name, desc)
                .build())
            .extractMethod(extractSpec(parameter, name)
                .addCode(CodeBlock.builder()
                    .addStatement("return $L.value($L).asSingle($L)",
                        var, PARAMETERS, asKeyType(parameter.asType()))
                    .build())
                .build())
            .build();
    }

    private CommandParamInfo injectableValue(VariableElement parameter) {
        String name = parameter.getSimpleName().toString();
        return CommandParamInfo.builder()
            .extractMethod(extractSpec(parameter, name)
                .addCode(CodeBlock.builder()
                    .addStatement("return $L.injectedValue($L)",
                        PARAMETERS, asKeyType(parameter.asType()))
                    .build())
                .build())
            .build();
    }

    private static TypeSpec asKeyType(TypeMirror mirror) {
        return TypeSpec.anonymousClassBuilder("")
            .superclass(ParameterizedTypeName.get(
                ClassName.get(Key.class),
                TypeName.get(mirror)
            ))
            .build();
    }

    private String getDesc(VariableElement parameter) {
        return MoreElements.getAnnotationMirror(parameter, Desc.class).toJavaUtil()
            .map(d -> (String) getAnnotationValue(d, "value").getValue())
            .orElseThrow(() -> new IllegalStateException("No description!"));
    }

    List<CommandParamInfo> getParams() {
        return method.getParameters().stream()
            .map(this::getParam)
            .collect(toImmutableList());
    }

    private CommandParamInfo getParam(VariableElement parameter) {
        ImmutableList<ParamTransform> transforms =
            ANNOTATION_TRANSFORMS.entrySet().stream()
                .filter(e -> isAnnotationPresent(parameter, e.getKey()))
                .map(Map.Entry::getValue)
                .collect(toImmutableList());
        checkState(transforms.size() <= 1,
            "Too many transforms applicable. Did you add conflicting annotations?");
        ParamTransform transform = transforms.isEmpty() ? this::injectableValue : transforms.get(0);
        return transform.createPartInfo(parameter);
    }
}
