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

import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import org.enginehub.piston.annotation.CommandContainer
import javax.tools.JavaFileObject
import kotlin.reflect.KClass


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

inline fun <reified T> className(): ClassName = ClassName.get(T::class.java)

fun ClassName.parametrize(vararg arguments: TypeName): ParameterizedTypeName =
    ParameterizedTypeName.get(this, *arguments)
