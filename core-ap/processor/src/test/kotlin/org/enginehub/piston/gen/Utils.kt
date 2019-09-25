package org.enginehub.piston.gen

import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import org.enginehub.piston.annotation.CommandContainer
import javax.tools.JavaFileObject


const val PACKAGE = "eh"

fun commands(name: String, specs: List<MethodSpec>): JavaFileObject {
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

fun compiler() = Compiler.javac().withProcessors(CommandProcessor())!!
