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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.enginehub.piston.util.SafeName;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.Streams.concat;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.enginehub.piston.util.CodeBlockUtil.listForGen;
import static org.enginehub.piston.util.CodeBlockUtil.stringListForGen;

/**
 * Class that handles the generation of command registration classes.
 *
 * <p>
 * These classes are named {@code [CommandContainer class name] + "Registration"}. They are
 * guaranteed to have one method with signature {@code public void register(CommandManager)}.
 * Generated methods will be used to convert the annotation-based configuration into a runtime
 * configuration, allowing for type-safe and efficient implementation while maintaining the ease
 * of annotation-based configuration.
 * </p>
 */
class CommandRegistrationGenerator {
    private static final ParameterSpec COMMAND_PARAMETERS_SPEC
        = ParameterSpec.builder(CommandParameters.class, "parameters").build();
    private final RegistrationInfo info;
    private final List<RequiredVariable> injectedVariables;

    CommandRegistrationGenerator(RegistrationInfo info) {
        this.info = info;
        this.injectedVariables = concat(
            cmdsFlatMap(i -> i.getInjectedVariables().stream()),
            additionalVariables(info)
        ).collect(toList());
    }

    private <T> Stream<T> cmdsFlatMap(Function<CommandInfo, Stream<T>> map) {
        return info.getCommands().stream().flatMap(map);
    }

    private static Stream<RequiredVariable> additionalVariables(RegistrationInfo info) {
        Stream.Builder<RequiredVariable> b = Stream.builder();
        b.add(RequiredVariable.builder()
            .type(ClassName.get(CommandManager.class))
            .name(ReservedVariables.COMMAND_MANAGER)
            .build());
        if (!info.getCommands().stream()
            .allMatch(CommandRegistrationGenerator::isCommandStatic)) {
            // we need the instance too
            b.add(RequiredVariable.builder()
                .type(info.getTargetClassName())
                .name(ReservedVariables.CONTAINER_INSTANCE)
                .build());
        }
        return b.build();
    }

    private Stream<RequiredVariable> getInjectedVariables() {
        return injectedVariables.stream();
    }

    private static boolean isCommandStatic(CommandInfo info) {
        return info.getCommandMethod().getModifiers().contains(Modifier.STATIC);
    }

    void generate(Element originalElement, String pkgName, Filer filer) throws IOException {
        TypeSpec.Builder spec = TypeSpec.classBuilder(info.getName())
            .addOriginatingElement(originalElement);

        if (info.getClassVisibility() != null) {
            spec.addModifiers(info.getClassVisibility());
        }

        spec.addFields(generateFields());
        spec.addMethod(generateConstructor());
        spec.addMethods(generateConditionMethods());
        spec.addMethods(generateCommandBindings());
        spec.addMethods(getParameterMethods());

        JavaFile.builder(pkgName, spec.build())
            .indent("    ")
            .addFileComment("Generated by $L on $L", getClass().getName(), Instant.now().toString())
            .build()
            .writeTo(filer);
    }

    private Iterable<MethodSpec> getParameterMethods() {
        return cmdsFlatMap(cmd -> cmd.getParams().stream())
            .map(CommandParamInfo::getExtractMethod)
            .collect(toList());
    }

    private Iterable<FieldSpec> generateFields() {
        return concat(getInjectedVariables(), cmdsFlatMap(cmd -> cmd.getDeclaredFields().stream()))
            .map(var -> FieldSpec.builder(
                var.getType(), var.getName(),
                Modifier.PRIVATE, Modifier.FINAL
            ).build())
            .collect(toList());
    }

    private MethodSpec generateConstructor() {
        List<ParameterSpec> params = getInjectedVariables()
            .map(var -> ParameterSpec.builder(var.getType(), var.getName())
                .addAnnotations(var.getAnnotations())
                .build())
            .collect(toList());
        CodeBlock.Builder body = getInjectedVariables()
            .map(var -> CodeBlock.of("this.$1L = $1L;\n", var.getName()))
            .reduce(CodeBlock.of(""), (a, b) -> a.toBuilder().add(b).build())
            .toBuilder();
        MethodSpec.Builder constr = MethodSpec.constructorBuilder();
        if (info.getJavaxInjectClassName() != null) {
            constr.addAnnotation(info.getJavaxInjectClassName());
        }

        if (info.getClassVisibility() != null) {
            constr.addModifiers(info.getClassVisibility());
        }

        for (CommandInfo cmd : info.getCommands()) {
            cmd.getParams().stream()
                .map(CommandParamInfo::getConstruction)
                .filter(Objects::nonNull)
                .forEach(body::add);
            body.add(generateRegisterCommandCode(cmd));
        }
        return constr
            .addParameters(params)
            .addCode(body.build())
            .build();
    }

    private CodeBlock generateRegisterCommandCode(CommandInfo cmd) {
        // Workaround no `addStatement` for now, see:
        // https://github.com/square/javapoet/issues/711
        return CodeBlock.builder()
            .add("$L.register($S, $L);\n", ReservedVariables.COMMAND_MANAGER, cmd.getName(),
                generateRegistrationLambda(cmd))
            .build();
    }

    private CodeBlock generateRegistrationLambda(CommandInfo cmd) {
        CodeBlock.Builder lambda = CodeBlock.builder()
            .add("b -> {\n").indent();
        lambda.addStatement("b.aliases($L)", stringListForGen(cmd.getAliases().stream()));
        lambda.addStatement("b.description($S)", cmd.getDescription());
        cmd.getFooter().ifPresent(footer ->
            lambda.addStatement("b.footer($S)", footer)
        );
        lambda.addStatement("b.parts($L)", listForGen(cmd.getParams().stream().map(
            CommandParamInfo::getPartVariable
        ).filter(Objects::nonNull)));
        lambda.addStatement("b.action(this::$L)", SafeName.from(cmd.getName()));
        if (cmd.getCondition().isPresent()) {
            lambda.addStatement("b.condition(this::$L)", conditionMethodName(cmd));
        }
        return lambda.unindent().add("}").build();
    }

    private Iterable<MethodSpec> generateConditionMethods() {
        return info.getCommands().stream()
            .filter(c -> c.getCondition().isPresent())
            .map(c -> MethodSpec.methodBuilder(conditionMethodName(c))
                .addModifiers(Modifier.PRIVATE)
                .returns(boolean.class)
                .addCode(c.getCondition().get())
                .build()
            )
            .collect(toList());
    }

    private String conditionMethodName(CommandInfo c) {
        return SafeName.from(c.getName()) + "Condition";
    }

    private Iterable<MethodSpec> generateCommandBindings() {
        return info.getCommands().stream()
            .map(this::generateCommandBinding)
            .collect(toList());
    }

    private MethodSpec generateCommandBinding(CommandInfo commandInfo) {
        MethodSpec.Builder spec = MethodSpec.methodBuilder(SafeName.from(commandInfo.getName()))
            .addModifiers(Modifier.PRIVATE)
            .returns(int.class)
            .addParameter(COMMAND_PARAMETERS_SPEC);

        CodeBlock callCommandMethod = generateCallCommandMethod(commandInfo);
        TypeName rawReturnType = TypeName.get(commandInfo.getCommandMethod().getReturnType()).unbox();
        if (TypeName.INT.equals(rawReturnType)) {
            // call the method, return what it does
            spec.addCode(CodeBlock.builder()
                .addStatement("return $L", callCommandMethod)
                .build());
        } else {
            // call the method, return 1
            spec.addCode(CodeBlock.builder()
                .addStatement(callCommandMethod)
                .addStatement("return 1")
                .build());
        }
        return spec.build();
    }

    private CodeBlock generateCallCommandMethod(CommandInfo commandInfo) {
        CodeBlock target;
        if (commandInfo.getCommandMethod().getModifiers().contains(Modifier.STATIC)) {
            // call it statically
            target = CodeBlock.of("$T", info.getTargetClassName());
        } else {
            // use the instance
            target = CodeBlock.of("$L", ReservedVariables.CONTAINER_INSTANCE);
        }

        String args = commandInfo.getParams().stream()
            .map(param -> CodeBlock.of("this.$L($L)",
                param.getExtractMethod().name,
                ReservedVariables.PARAMETERS).toString())
            .collect(joining(", "));
        return CodeBlock.of("$L.$L($L)",
            target,
            commandInfo.getCommandMethod().getSimpleName(),
            args);
    }

}