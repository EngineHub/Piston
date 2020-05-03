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
