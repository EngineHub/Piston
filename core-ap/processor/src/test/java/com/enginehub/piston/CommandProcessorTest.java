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
import java.io.IOException;
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
    void generateSimpleCommand() throws IOException {
        Compilation compilation = javac()
            .withProcessors(new CommandProcessor())
            .compile(getSimpleCommandSource());
        assertThat(compilation).succeededWithoutWarnings();
        JavaFileObject gen = compilation.generatedSourceFile("pkg.SimpleCommandRegistration")
            .orElseThrow(AssertionError::new);
        System.err.println("===> Source:");
        System.err.println(getSimpleCommandSource().getCharContent(true));
        System.err.println("===> Generated:");
        System.err.println(gen.getCharContent(true));
    }

    private JavaFileObject getSimpleCommandSource() {
        return classSource("pkg", "SimpleCommand", b -> b
            .addAnnotation(CommandContainer.class)
            .addModifiers(Modifier.PUBLIC)
            .addMethod(MethodSpec.methodBuilder("aSimpleCommand")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "injected")
                .addAnnotation(AnnotationSpec.builder(Command.class)
                    .addMember("name", "$S", "simple")
                    .addMember("desc", "$S", "description")
                    .build())
                .build()));
    }

}
