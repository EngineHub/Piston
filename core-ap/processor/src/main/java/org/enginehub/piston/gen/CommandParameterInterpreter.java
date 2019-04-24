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
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.CommandValue;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.annotation.param.Switch;
import org.enginehub.piston.gen.util.ProcessingException;
import org.enginehub.piston.gen.util.TypeNameUtil;
import org.enginehub.piston.gen.value.CommandParamInfo;
import org.enginehub.piston.gen.value.ExtractSpec;
import org.enginehub.piston.gen.value.ReservedNames;
import org.enginehub.piston.inject.InjectAnnotation;
import org.enginehub.piston.internal.RegistrationUtil;
import org.enginehub.piston.part.ArgAcceptingCommandFlag;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.CommandParts;
import org.enginehub.piston.part.NoArgCommandFlag;
import org.enginehub.piston.util.CaseHelper;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.auto.common.MoreElements.asType;
import static com.google.auto.common.MoreElements.getAnnotationMirror;
import static com.google.auto.common.MoreElements.isAnnotationPresent;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.enginehub.piston.gen.util.AnnoValueExtraction.getList;
import static org.enginehub.piston.gen.util.AnnoValueExtraction.getValue;
import static org.enginehub.piston.gen.util.CodeBlockUtil.listForGen;
import static org.enginehub.piston.gen.util.CodeBlockUtil.stringListForGen;

class CommandParameterInterpreter {

    private interface ParamTransform {

        CommandParamInfo createPartInfo(VariableElement parameter);

    }

    private static boolean isUnconverted(ProcessingEnvironment env, VariableElement parameter) {
        TypeMirror commandValue = env.getElementUtils().getTypeElement(CommandValue.class.getCanonicalName()).asType();
        return env.getTypeUtils().isAssignable(parameter.asType(), commandValue);
    }

    private final Map<Class<? extends Annotation>, ParamTransform> ANNOTATION_TRANSFORMS = ImmutableMap.of(
        Arg.class, this::argTransform,
        ArgFlag.class, this::argFlagTransform,
        Switch.class, this::switchTransform
    );
    private final ExecutableElement method;
    private final GenerationSupport generationSupport;
    private final ProcessingEnvironment env;

    CommandParameterInterpreter(ExecutableElement method,
                                GenerationSupport generationSupport,
                                ProcessingEnvironment env) {
        this.method = method;
        this.generationSupport = generationSupport;
        this.env = env;
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
        CodeBlock.Builder construction = CodeBlock.builder()
            .add("$T.arg($S, $S)\n" +
                    ".defaultsTo($L)\n",
                CommandParts.class, name, desc,
                stringListForGen(defaults.stream()));
        addArgTypes(parameter, construction);
        if (getValue(parameter, arg, "variable", boolean.class)) {
            construction.add(".variable(true)\n");
        }
        construction.add(".build()");
        return CommandParamInfo.builder()
            .name(parameter.getSimpleName() + "Part")
            .type(TypeName.get(CommandArgument.class))
            .construction(construction.build())
            .extractSpec(getArgExtractSpec(parameter))
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
        CodeBlock.Builder construction = CodeBlock.builder()
            .add("$T.flag('$L', $S)\n" +
                    ".withRequiredArg()\n" +
                    ".argNamed($S)\n" +
                    ".defaultsTo($L)\n",
                CommandParts.class, name, desc,
                argName,
                stringListForGen(defaults.stream()));
        addArgTypes(parameter, construction);
        construction.add(".build()");
        return CommandParamInfo.builder()
            .name(parameter.getSimpleName() + "Part")
            .type(TypeName.get(ArgAcceptingCommandFlag.class))
            .construction(construction.build())
            .extractSpec(getArgExtractSpec(parameter))
            .build();
    }

    private void addArgTypes(VariableElement parameter, CodeBlock.Builder construction) {
        if (!isUnconverted(env, parameter)) {
            CodeBlock keyRef;
            TypeMirror parameterType = parameter.asType();
            if (isMultiCompatibleType(parameterType)) {
                keyRef = asKeyType(parameter,
                    TypeNameUtil.firstTypeArg(TypeName.get(parameterType)));
            } else {
                keyRef = asKeyType(parameter);
            }
            construction.add(".ofTypes($L)\n", listForGen(Stream.of(keyRef)));
        }
    }

    private ExtractSpec getArgExtractSpec(VariableElement parameter) {
        TypeMirror parameterType = parameter.asType();
        return ExtractSpec.builder()
            .name(parameter.getSimpleName().toString())
            .type(TypeName.get(parameterType))
            .extractMethodBody(var -> {
                CodeBlock.Builder builder = CodeBlock.builder();
                if (isUnconverted(env, parameter)) {
                    builder.addStatement("return $L.value($L)",
                        var, ReservedNames.PARAMETERS);
                } else if (isMultiCompatibleType(parameterType)) {
                    // Parameter is a collection type
                    builder.addStatement("return $L.value($L).asMultiple($L)",
                        var, ReservedNames.PARAMETERS, asKeyType(
                            parameter,
                            TypeNameUtil.firstTypeArg(TypeName.get(parameterType))
                        ));
                } else {
                    builder.addStatement("return $L.value($L).asSingle($L)",
                        var, ReservedNames.PARAMETERS, asKeyType(parameter));
                }
                return builder.build();
            })
            .build();
    }

    private boolean isMultiCompatibleType(TypeMirror mirror) {
        TypeMirror immutableList = env.getElementUtils().getTypeElement(ImmutableList.class.getCanonicalName())
            .asType();
        Element paramAsElement = env.getTypeUtils().asElement(mirror);
        boolean isObject = paramAsElement != null && Object.class.getName().contentEquals(
            asType(paramAsElement).getQualifiedName()
        );
        TypeMirror erasedType = env.getTypeUtils().erasure(mirror);
        return !isObject && env.getTypeUtils().isAssignable(immutableList, erasedType);
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
                .extractMethodBody(var -> {
                    CodeBlock paramKey = asKeyType(parameter);
                    return CodeBlock.builder()
                        .addStatement("return $T.requireOptional($L, $S, $L.injectedValue($L))",
                            RegistrationUtil.class,
                            paramKey,
                            parameter.getSimpleName(),
                            ReservedNames.PARAMETERS, paramKey)
                        .build();
                })
                .build())
            .build();
    }

    private CodeBlock asKeyType(VariableElement mirror) {
        return asKeyType(mirror, TypeName.get(mirror.asType()));
    }

    private CodeBlock asKeyType(VariableElement mirror, TypeName typeName) {
        ImmutableList<AnnotationMirror> firstAnnotation = mirror.getAnnotationMirrors().stream()
            .filter(am -> {
                TypeElement annoType = asType(am.getAnnotationType().asElement());
                return getAnnotationMirror(annoType, InjectAnnotation.class).isPresent();
            })
            .collect(toImmutableList());
        if (firstAnnotation.size() > 1) {
            throw new ProcessingException("Too many binding annotations. Only one is allowed.")
                .withElement(mirror)
                .withAnnotation(firstAnnotation.get(1));
        }
        return generationSupport.requestKey(
            typeName,
            firstAnnotation.stream().map(AnnotationSpec::get).findFirst().orElse(null)
        );
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
