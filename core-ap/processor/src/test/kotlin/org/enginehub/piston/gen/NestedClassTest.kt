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
import com.google.testing.compile.JavaFileObjects
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import org.enginehub.piston.annotation.Command
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


@DisplayName("core-ap can generate from a nested class")
class NestedClassTest {
    @DisplayName("a basic command class")
    @Test
    fun basicTest() {
        val nestedClass = commandsSpec("BasicNested", listOf(
            MethodSpec.methodBuilder("noArg")
                .addAnnotation(AnnotationSpec.builder(Command::class.java)
                    .addMember("name", "\$S", "noArgument")
                    .addMember("desc", "\$S", "DESCRIPTION")
                    .build())
                .returns(Void.TYPE)
                .addException(Exception::class.java)
                .build()
        ))
        val outerClass = TypeSpec.classBuilder("Outer")
            .addType(nestedClass)
            .build()


        val compilation = compiler().compile(outerClass.toFileInPackage())
        assertThat(compilation)
            .succeededWithoutWarnings()
        assertThat(compilation)
            .generatedSourceFile("$PACKAGE.Outer_BasicNestedRegistration")
            .hasSourceEquivalentTo(JavaFileObjects.forResource("gen/Outer_BasicNestedRegistration.java"))
    }
}
