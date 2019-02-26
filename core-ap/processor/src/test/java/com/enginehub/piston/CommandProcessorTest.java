package com.enginehub.piston;

import com.enginehub.piston.annotation.Command;
import com.enginehub.piston.annotation.CommandContainer;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;
import java.util.function.Consumer;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

@DisplayName("A command processor")
class CommandProcessorTest {

    private static JavaFileObject classSource(String pkg,
                                              String name,
                                              Consumer<TypeSpec.Builder> content) {
        TypeSpec.Builder b = TypeSpec.classBuilder(name);
        content.accept(b);
        return JavaFileObjects.forSourceString(pkg + "." + name,
            JavaFile.builder(pkg, b.build())
                .indent("    ")
                .build()
                .toString());
    }

    @Test
    @DisplayName("can generate a simple command")
    void generateSimpleCommand() {
        Compilation compilation = javac()
            .withProcessors(new CommandProcessor())
            .compile(getSimpleCommandSource());
        assertThat(compilation).succeededWithoutWarnings();
        assertThat(compilation)
            .generatedSourceFile("pkg.SimpleCommandRegistration")
            .hasSourceEquivalentTo(JavaFileObjects.forSourceString("pkg.SimpleCommandRegistration", ""));
    }

    private JavaFileObject getSimpleCommandSource() {
        return classSource("pkg", "SimpleCommand", b -> b
            .addAnnotation(CommandContainer.class)
            .addMethod(MethodSpec.methodBuilder("aSimpleCommand")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Command.class)
                    .addMember("name", "$S", "simple")
                    .addMember("desc", "$S", "description")
                    .build())
                .build()));
    }

}
