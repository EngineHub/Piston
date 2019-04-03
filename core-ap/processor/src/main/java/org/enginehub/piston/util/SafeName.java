/*
 * Piston, a flexible command management system.
 * Copyright (C) EngineHub <http://www.enginehub.com>
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

package org.enginehub.piston.util;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

/**
 * Utility for making Java-safe names from free-form Unicode.
 *
 * <p>
 *     Safe names are made by replacing non-safe characters with underscores.
 * </p>
 */
public class SafeName {

    public static String from(String unsafe) {
        StringBuilder result = new StringBuilder(unsafe.length());
        int firstCp = unsafe.codePointAt(0);
        if (!Character.isJavaIdentifierStart(firstCp)) {
            firstCp = '_';
        }
        result.appendCodePoint(firstCp);
        unsafe.codePoints()
            .skip(1)
            .map(cp -> Character.isJavaIdentifierPart(cp) ? cp : '_')
            .forEachOrdered(result::appendCodePoint);
        return result.toString();
    }

    public static String getNameAsIdentifier(TypeName typeName) {
        return from(CaseHelper.titleToCamel(getNameAsIdentifierRaw(typeName).toString()));
    }

    private static CharSequence getNameAsIdentifierRaw(TypeName typeName) {
        if (typeName instanceof ClassName) {
            // good, just the raw name works here
            return ((ClassName) typeName).simpleName();
        } else if (typeName instanceof ParameterizedTypeName) {
            // append the type parameters
            ParameterizedTypeName pt = (ParameterizedTypeName) typeName;
            ClassName raw = pt.rawType;
            StringBuilder result = new StringBuilder(
                getNameAsIdentifierRaw(raw)
            );
            for (TypeName typeArgument : pt.typeArguments) {
                result.append(getNameAsIdentifierRaw(typeArgument));
            }
            return result;
        } else if (typeName instanceof ArrayTypeName) {
            // append Array to the name
            CharSequence base = getNameAsIdentifierRaw(((ArrayTypeName) typeName).componentType);
            return new StringBuilder(base).append("Array");
        }
        // just use toString() as a last resort
        return typeName.toString();
    }
}
