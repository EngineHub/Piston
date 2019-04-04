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

import com.google.auto.common.MoreElements;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.annotation.param.Switch;
import org.enginehub.piston.gen.util.ProcessingException;
import org.enginehub.piston.gen.value.CommandParamInfo;
import org.enginehub.piston.gen.value.ExtractSpec;
import org.enginehub.piston.gen.value.ReservedNames;
import org.enginehub.piston.part.ArgAcceptingCommandFlag;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.CommandParts;
import org.enginehub.piston.part.NoArgCommandFlag;
import org.enginehub.piston.util.CaseHelper;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import static com.google.auto.common.MoreElements.isAnnotationPresent;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.enginehub.piston.gen.util.AnnoValueExtraction.getList;
import static org.enginehub.piston.gen.util.AnnoValueExtraction.getValue;
import static org.enginehub.piston.gen.util.CodeBlockUtil.stringListForGen;

class CommandParameterInterpreter {

    private interface ParamTransform {

        CommandParamInfo createPartInfo(VariableElement parameter);

    }

    private final Map<Class<? extends Annotation>, ParamTransform> ANNOTATION_TRANSFORMS = ImmutableMap.of(
        Arg.class, this::argTransform,
        ArgFlag.class, this::argFlagTransform,
        Switch.class, this::switchTransform
    );
    private final ExecutableElement method;
    private final GenerationSupport generationSupport;

    CommandParameterInterpreter(ExecutableElement method, GenerationSupport generationSupport) {
        this.method = method;
        this.generationSupport = generationSupport;
    }

    private MethodSpec.Builder extractSpec(VariableElement param, String name) {
        return MethodSpec.methodBuilder(generationSupport.requestMethodName("extract"
            + CaseHelper.camelToTitle(name)))
            .addModifiers(Modifier.PRIVATE)
            .addParameter(CommandParameters.class, ReservedNames.PARAMETERS)
            .returns(TypeName.get(param.asType()));
    }

    private CommandParamInfo argTransform(VariableElement parameter) {
        AnnotationMirror arg = MoreElements.getAnnotationMirror(parameter, Arg.class)
            .toJavaUtil().orElseThrow(() ->
                new ProcessingException("Missing Arg annotation").withElement(parameter));
        String name = getValue(parameter, arg, "name", String.class);
        if (name.equals(Arg.NAME_IS_PARAMETER_NAME)) {
            name = parameter.getSimpleName().toString();
        }
        String desc = getValue(parameter, arg, "desc", String.class);
        List<String> defaults = getList(parameter, arg, "def", String.class);
        return CommandParamInfo.builder()
            .name(parameter.getSimpleName() + "Part")
            .type(TypeName.get(CommandArgument.class))
            .construction(
                CodeBlock.of(
                    "$T.arg($S, $S)\n" +
                        ".defaultsTo($L)\n" +
                        ".build()",
                    CommandParts.class, name, desc,
                    stringListForGen(defaults.stream())))
            .extractSpec(ExtractSpec.builder()
                .name(parameter.getSimpleName().toString())
                .type(TypeName.get(parameter.asType()))
                .extractMethodBody(var -> CodeBlock.builder()
                    .addStatement("return $L.value($L).asSingle($L)",
                        var, ReservedNames.PARAMETERS, asKeyType(parameter.asType()))
                    .build())
                .build())
            .build();
    }

    private CommandParamInfo argFlagTransform(VariableElement parameter) {
        AnnotationMirror arg = MoreElements.getAnnotationMirror(parameter, ArgFlag.class)
            .toJavaUtil().orElseThrow(() ->
                new ProcessingException("Missing Arg annotation").withElement(parameter));
        char name = getValue(parameter, arg, "name", char.class);
        String argName = getValue(parameter, arg, "argName", String.class);
        if (argName.equals(ArgFlag.ARG_NAME_IS_PARAMETER_NAME)) {
            argName = parameter.getSimpleName().toString();
        }
        String desc = getValue(parameter, arg, "desc", String.class);
        List<String> defaults = getList(parameter, arg, "def", String.class);
        return CommandParamInfo.builder()
            .name(parameter.getSimpleName() + "Part")
            .type(TypeName.get(ArgAcceptingCommandFlag.class))
            .construction(CodeBlock.of(
                "$T.flag('$L', $S)\n" +
                    ".withRequiredArg()\n" +
                    ".argNamed($S)\n" +
                    ".defaultsTo($L)\n" +
                    ".build()",
                CommandParts.class, name, desc,
                argName,
                stringListForGen(defaults.stream())))
            .extractSpec(ExtractSpec.builder()
                .name(parameter.getSimpleName().toString())
                .type(TypeName.get(parameter.asType()))
                .extractMethodBody(var -> CodeBlock.builder()
                    .addStatement("return $L.value($L).asSingle($L)",
                        var, ReservedNames.PARAMETERS, asKeyType(parameter.asType()))
                    .build())
                .build())
            .build();
    }

    private CommandParamInfo switchTransform(VariableElement parameter) {
        checkState(TypeName.BOOLEAN.equals(TypeName.get(parameter.asType())),
            "Non-boolean parameter annotated with @Switch");
        AnnotationMirror switchAnno = MoreElements.getAnnotationMirror(parameter, Switch.class)
            .toJavaUtil().orElseThrow(() ->
                new ProcessingException("Missing Switch annotation").withElement(parameter));
        char name = getValue(parameter, switchAnno, "name", char.class);
        String desc = getValue(parameter, switchAnno, "desc", String.class);
        return CommandParamInfo.builder()
            .name(parameter.getSimpleName() + "Part")
            .type(TypeName.get(NoArgCommandFlag.class))
            .construction(CodeBlock.of(
                "$T.flag('$L', $S).build()",
                CommandParts.class, name, desc))
            .extractSpec(ExtractSpec.builder()
                .name(parameter.getSimpleName().toString())
                .type(TypeName.get(parameter.asType()))
                .extractMethodBody(var -> CodeBlock.builder()
                    .addStatement("return $L.in($L)",
                        var, ReservedNames.PARAMETERS)
                    .build())
                .build())
            .build();
    }

    private CommandParamInfo injectableValue(VariableElement parameter) {
        String name = parameter.getSimpleName().toString();
        return CommandParamInfo.builder()
            .extractSpec(ExtractSpec.builder()
                .name(parameter.getSimpleName().toString())
                .type(TypeName.get(parameter.asType()))
                .extractMethodBody(var -> CodeBlock.builder()
                    .addStatement("return $L.injectedValue($L)",
                        ReservedNames.PARAMETERS, asKeyType(parameter.asType()))
                    .build())
                .build())
            .build();
    }

    private CodeBlock asKeyType(TypeMirror mirror) {
        return generationSupport.requestKey(TypeName.get(mirror));
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
        if (transforms.size() > 1) {
            throw new ProcessingException(
                "Too many transforms applicable. Did you add conflicting annotations?")
                .withElement(parameter);
        }
        ParamTransform transform = transforms.isEmpty() ? this::injectableValue : transforms.get(0);
        return transform.createPartInfo(parameter);
    }
}