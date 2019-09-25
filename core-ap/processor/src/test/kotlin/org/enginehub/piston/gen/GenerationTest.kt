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

package org.enginehub.piston.gen

import com.google.testing.compile.CompilationSubject.assertThat
import com.google.testing.compile.Compiler.javac
import com.google.testing.compile.JavaFileObjects
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import org.enginehub.piston.annotation.Command
import org.enginehub.piston.annotation.CommandContainer
import org.enginehub.piston.annotation.param.Arg
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import javax.tools.JavaFileObject

private const val PACKAGE = "eh"

private fun commands(name: String, specs: List<MethodSpec>): JavaFileObject {
    val source = JavaFile.builder(PACKAGE, TypeSpec.classBuilder(name)
        .addAnnotation(CommandContainer::class.java)
        .addMethods(specs)
        .build())
        .build()
        .toString()
    return JavaFileObjects.forSourceString(
        "$PACKAGE.$name", source
    )
}

private fun compiler() = javac().withProcessors(CommandProcessor())

@DisplayName("core-ap can generate")
@Execution(ExecutionMode.CONCURRENT)
class GenerationTest {

    @DisplayName("a no argument command")
    @Test
    fun generatesNoArg() {
        val commands = commands("NoArg", listOf(
            MethodSpec.methodBuilder("noArg")
                .addAnnotation(AnnotationSpec.builder(Command::class.java)
                    .addMember("name", "\$S", "noArgument")
                    .addMember("desc", "\$S", "DESCRIPTION")
                    .build())
                .returns(Void.TYPE)
                .build()
        ))
        val compilation = compiler().compile(commands)
        assertThat(compilation)
            .succeededWithoutWarnings()
        assertThat(compilation)
            .generatedSourceFile("$PACKAGE.NoArgRegistration")
            .hasSourceEquivalentTo(JavaFileObjects.forResource("gen/NoArgRegistration.java"))
    }

    @DisplayName("a one argument (String) command")
    @Test
    fun generatesOneStringArg() {
        val commands = commands("StringArg", listOf(
            MethodSpec.methodBuilder("stringArg")
                .addAnnotation(AnnotationSpec.builder(Command::class.java)
                    .addMember("name", "\$S", "stringArgument")
                    .addMember("desc", "\$S", "DESCRIPTION")
                    .build())
                .returns(Void.TYPE)
                .addParameter(
                    ParameterSpec.builder(String::class.java, "arg")
                        .addAnnotation(AnnotationSpec.builder(Arg::class.java)
                            .addMember("desc", "\$S", "ARG DESCRIPTION")
                            .build())
                        .build()
                )
                .build()
        ))
        val compilation = compiler().compile(commands)
        assertThat(compilation)
            .succeededWithoutWarnings()
        assertThat(compilation)
            .generatedSourceFile("$PACKAGE.StringArgRegistration")
            .hasSourceEquivalentTo(JavaFileObjects.forResource("gen/StringArgRegistration.java"))
    }

    @DisplayName("a one argument (int) command")
    @Test
    fun generatesOneIntArg() {
        val commands = commands("IntArg", listOf(
            MethodSpec.methodBuilder("intArg")
                .addAnnotation(AnnotationSpec.builder(Command::class.java)
                    .addMember("name", "\$S", "intArgument")
                    .addMember("desc", "\$S", "DESCRIPTION")
                    .build())
                .returns(TypeName.INT)
                .addParameter(
                    ParameterSpec.builder(TypeName.INT, "arg")
                        .addAnnotation(AnnotationSpec.builder(Arg::class.java)
                            .addMember("desc", "\$S", "ARG DESCRIPTION")
                            .build())
                        .build()
                )
                .addStatement("return arg")
                .build()
        ))
        val compilation = compiler().compile(commands)
        assertThat(compilation)
            .succeededWithoutWarnings()
        assertThat(compilation)
            .generatedSourceFile("$PACKAGE.IntArgRegistration")
            .hasSourceEquivalentTo(JavaFileObjects.forResource("gen/IntArgRegistration.java"))
    }

}