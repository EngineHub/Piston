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

package com.enginehub.piston.util;

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

}
