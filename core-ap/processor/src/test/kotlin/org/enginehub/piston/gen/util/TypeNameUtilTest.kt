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

package org.enginehub.piston.gen.util

import com.google.common.primitives.Primitives
import com.google.common.truth.Truth
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.TypeName
import org.enginehub.piston.gen.className
import org.enginehub.piston.gen.parametrize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("TypeNameUtil")
class TypeNameUtilTest {

    private inline fun <reified R> assertRawType(nonRaw: TypeName) {
        assertRawType(className<R>(), TypeNameUtil.rawType(nonRaw))
    }

    private fun assertRawType(raw: TypeName, nonRaw: TypeName) {
        assertEquals(raw, TypeNameUtil.rawType(nonRaw))
    }

    @DisplayName("can resolve raw type from ClassName")
    @Test
    fun rawTypeFromClassName() {
        assertRawType<String>(className<String>())
    }

    @DisplayName("can resolve raw type from ArrayTypeName")
    @Test
    fun rawTypeFromArrayTypeName() {
        assertRawType(
            ArrayTypeName.of(className<String>()),
            ArrayTypeName.of(className<String>())
        )
        assertRawType(
            ArrayTypeName.of(className<Collection<*>>()),
            ArrayTypeName.of(className<Collection<*>>().parametrize(className<String>()))
        )
    }

    @DisplayName("can resolve raw type from ParameterizedTypeName")
    @Test
    fun rawTypeFromParameterizedTypeName() {
        assertRawType<Collection<*>>(
            className<Collection<*>>().parametrize(className<String>())
        )
    }

    @DisplayName("can resolve raw type from primitive TypeName")
    @Test
    fun rawTypeFromPrimitiveTypeName() {
        Primitives.allPrimitiveTypes()
            .filter { it != Void.TYPE }
            .map { primitiveType ->
                assertRawType(TypeName.get(primitiveType), TypeName.get(primitiveType))
            }
    }

    @DisplayName("cannot resolve raw type from void TypeName")
    @Test
    fun noRawTypeFromVoidTypeName() {
        val ex = assertThrows<IllegalArgumentException> {
            TypeNameUtil.rawType(TypeName.VOID)
        }
        Truth.assertThat(ex).hasMessageThat().contains("Not able to create a raw type")
        Truth.assertThat(ex).hasMessageThat().contains("void")
    }

    @DisplayName("can resolve first type argument from ParameterizedTypeName")
    @Test
    fun firstTypeArgFromParameterizedTypeName() {
        assertEquals(className<String>(), TypeNameUtil.firstTypeArg(
            className<Collection<*>>().parametrize(className<String>())
        ))
    }

    @DisplayName("resolves Object as first type argument if not parameterized")
    @Test
    fun firstTypeArgFromOtherTypeName() {
        assertEquals(className<Any>(), TypeNameUtil.firstTypeArg(
            className<String>()
        ))
        assertEquals(className<Any>(), TypeNameUtil.firstTypeArg(
            TypeName.INT
        ))
        assertEquals(className<Any>(), TypeNameUtil.firstTypeArg(
            TypeName.VOID
        ))
    }

}
