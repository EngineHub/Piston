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

}
