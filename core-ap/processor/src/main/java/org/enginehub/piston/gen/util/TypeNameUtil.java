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

package org.enginehub.piston.gen.util;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

public class TypeNameUtil {

    public static TypeName rawType(TypeName typeName) {
        if (typeName instanceof ClassName) {
            return typeName;
        } else if (typeName instanceof ArrayTypeName) {
            return ArrayTypeName.of(rawType(((ArrayTypeName) typeName).componentType));
        } else if (typeName instanceof ParameterizedTypeName) {
            return ((ParameterizedTypeName) typeName).rawType;
        } else if (typeName.isPrimitive()) {
            return typeName;
        }
        throw new IllegalArgumentException("Not able to create a raw type from " +
            "'" + typeName + "' ("  + typeName.getClass() + ")");
    }

    public static TypeName firstTypeArg(TypeName typeName) {
        if (typeName instanceof ParameterizedTypeName) {
            return ((ParameterizedTypeName) typeName).typeArguments.get(0);
        }
        return TypeName.OBJECT;
    }

    private TypeNameUtil() {
    }

}
