package org.enginehub.piston.gen

import com.google.testing.compile.CompilationSubject.assertThat
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import org.enginehub.piston.CommandValue
import org.enginehub.piston.annotation.Command
import org.enginehub.piston.annotation.param.Arg
import org.enginehub.piston.annotation.param.ArgFlag
import org.enginehub.piston.annotation.param.Switch
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("core-ap fails")
class BadGenerationTest {
    @DisplayName("when given multiple @InjectAnnotations")
    @Test
    fun failMultipleInjectAnnotations() {
        val commands = commands("MultiInjectAnno", listOf(
            MethodSpec.methodBuilder("multiInject")
                .addAnnotation(AnnotationSpec.builder(Command::class.java)
                    .addMember("name", "\$S", "multiInject")
                    .addMember("desc", "\$S", "DESCRIPTION")
                    .build())
                .returns(Void.TYPE)
                .addParameter(
                    ParameterSpec.builder(CommandValue::class.java, "arg")
                        .addAnnotation(InjectAlpha::class.java)
                        .addAnnotation(InjectBeta::class.java)
                        .build()
                )
                .build()
        ))
        val compilation = compiler().compile(commands)
        assertThat(compilation).failed()
        assertThat(compilation)
            .hadErrorContaining("Too many binding annotations. Only one is allowed.")
            .inFile(commands)
            .onLineContaining("CommandValue arg")
    }

    @DisplayName("when given @Arg + @Switch")
    @Test
    fun failArgSwitchAnnotations() {
        val commands = commands("MultiArg", listOf(
            MethodSpec.methodBuilder("multiArg")
                .addAnnotation(AnnotationSpec.builder(Command::class.java)
                    .addMember("name", "\$S", "multiArg")
                    .addMember("desc", "\$S", "DESCRIPTION")
                    .build())
                .returns(Void.TYPE)
                .addParameter(
                    ParameterSpec.builder(CommandValue::class.java, "arg")
                        .addAnnotation(AnnotationSpec.builder(Arg::class.java)
                            .addMember("desc", "\$S", "ARG DESCRIPTION")
                            .build())
                        .addAnnotation(AnnotationSpec.builder(Switch::class.java)
                            .addMember("name", "'f'")
                            .addMember("desc", "\$S", "ARG DESCRIPTION")
                            .build())
                        .build()
                )
                .build()
        ))
        val compilation = compiler().compile(commands)
        assertThat(compilation).failed()
        assertThat(compilation)
            .hadErrorContaining("Too many transforms applicable. Did you add conflicting annotations?")
            .inFile(commands)
            .onLineContaining("CommandValue arg")
    }

    @DisplayName("when given @Switch + @ArgFlag")
    @Test
    fun failSwitchArgFlagAnnotations() {
        val commands = commands("MultiArg", listOf(
            MethodSpec.methodBuilder("multiArg")
                .addAnnotation(AnnotationSpec.builder(Command::class.java)
                    .addMember("name", "\$S", "multiArg")
                    .addMember("desc", "\$S", "DESCRIPTION")
                    .build())
                .returns(Void.TYPE)
                .addParameter(
                    ParameterSpec.builder(CommandValue::class.java, "arg")
                        .addAnnotation(AnnotationSpec.builder(Switch::class.java)
                            .addMember("name", "'f'")
                            .addMember("desc", "\$S", "ARG DESCRIPTION")
                            .build())
                        .addAnnotation(AnnotationSpec.builder(ArgFlag::class.java)
                            .addMember("name", "'f'")
                            .addMember("desc", "\$S", "ARG DESCRIPTION")
                            .build())
                        .build()
                )
                .build()
        ))
        val compilation = compiler().compile(commands)
        assertThat(compilation).failed()
        assertThat(compilation)
            .hadErrorContaining("Too many transforms applicable. Did you add conflicting annotations?")
            .inFile(commands)
            .onLineContaining("CommandValue arg")
    }
}
