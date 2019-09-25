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

package org.enginehub.piston.util;

import com.google.common.base.Splitter;

import java.util.stream.IntStream;

import static com.google.common.collect.Streams.concat;
import static com.google.common.collect.Streams.stream;
import static java.util.stream.Collectors.joining;

public class CaseHelper {

    // Splits on capital letters, without consuming the letter.
    // Regex explained:
    // (?<!^|\\s) -- negative lookbehind for start of line, prevents split if capital is the first
    // letter or start of a word
    // (?=\p{Lu}) -- positive lookahead for uppercase letters, according to Unicode.
    private static final Splitter CAPITAL_SPLITTER = Splitter.onPattern("(?<!^|\\s)(?=\\p{Lu})")
        .omitEmptyStrings();

    /**
     * Convert a string from title case to spaced lower case.
     *
     * <p>
     * Example: {@code "SomeClassName"} -> {@code "some class name"}.
     * </p>
     *
     * @param titleCase the string that is in title case
     * @return a string in spaced lower case
     */
    public static String titleToSpacedLower(String titleCase) {
        return stream(CAPITAL_SPLITTER.split(titleCase))
            .map(x -> {
                int firstCp = Character.toLowerCase(x.codePointAt(0));
                IntStream restCp = x.codePoints().skip(1);
                return concat(IntStream.of(firstCp), restCp)
                    .collect(() -> new StringBuilder(x.length()),
                        StringBuilder::appendCodePoint,
                        StringBuilder::append);
            }).collect(joining(" "));
    }

    /**
     * Convert a string from title case to camel case.
     *
     * <p>
     * Example: {@code "SomeClassName"} -> {@code "someClassName"}.
     * </p>
     *
     * @param titleCase the string that is in title case
     * @return a string in camel case
     */
    public static String titleToCamel(String titleCase) {
        int firstCp = Character.toLowerCase(titleCase.codePointAt(0));
        IntStream restCp = titleCase.codePoints().skip(1);
        return concat(IntStream.of(firstCp), restCp)
            .collect(() -> new StringBuilder(titleCase.length()),
                StringBuilder::appendCodePoint,
                StringBuilder::append).toString();
    }

    /**
     * Convert a string from camel case to title case.
     *
     * <p>
     * Example: {@code "someMethodName"} -> {@code "SomeMethodName"}.
     * </p>
     *
     * @param camelCase the string that is in camel case
     * @return a string in title case
     */
    public static String camelToTitle(String camelCase) {
        StringBuilder out = new StringBuilder(camelCase.length());
        int capital = Character.toUpperCase(camelCase.codePointAt(0));
        out.appendCodePoint(capital)
            .append(camelCase,
                camelCase.offsetByCodePoints(0, 1),
                camelCase.length());
        return out.toString();
    }
}
