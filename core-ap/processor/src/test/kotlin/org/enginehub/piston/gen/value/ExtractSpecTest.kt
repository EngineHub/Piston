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

package org.enginehub.piston.gen.value

import com.squareup.javapoet.CodeBlock
import org.enginehub.piston.gen.className
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

@DisplayName("An ExtractSpec")
@Execution(ExecutionMode.CONCURRENT)
class ExtractSpecTest {

    private val extractSpec = ExtractSpec.builder()
        .name("foo")
        .type(className<String>())
        .extractMethodBody { name -> CodeBlock.of("\$S", "My name is $name") }
        .build()

    @DisplayName("is equal to itself")
    @Test
    fun isEqualToItself() {
        assertEquals(extractSpec, extractSpec)
    }

    @DisplayName("is equal to a copy")
    @Test
    fun isEqualToCopy() {
        val copy = extractSpec.toBuilder().build()
        assertNotSame(extractSpec, copy)
        assertEquals(extractSpec, copy)
    }

    @DisplayName("is not equal to different name")
    @Test
    fun isNotEqualDifferentName() {
        val copy = extractSpec.toBuilder().name("diff").build()
        assertNotEquals(extractSpec, copy)
    }

    @DisplayName("is not equal to different type")
    @Test
    fun isNotEqualDifferentType() {
        val copy = extractSpec.toBuilder().type(className<Int>()).build()
        assertNotEquals(extractSpec, copy)
    }

    @DisplayName("is not equal to different body generator")
    @Test
    fun isNotEqualDifferentGenerator() {
        val copy = extractSpec.toBuilder().extractMethodBody { name ->
            CodeBlock.of("\$S", "I'm a different $name")
        }.build()
        assertNotEquals(extractSpec, copy)
    }

    @DisplayName("is not equal to different body generator")
    @Test
    fun isNotEqualDifferentClass() {
        assertNotEquals(extractSpec, "not an extract spec :)")
    }

}