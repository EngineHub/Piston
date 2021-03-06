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

package org.enginehub.piston.converter;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SuggestionHelper {

    public static List<String> limitByPrefix(Stream<String> choices, String input) {
        return choices.filter(byPrefix(input))
            .collect(Collectors.toList());
    }

    // intended for use as stream.filter(byPrefix(input))
    public static Predicate<String> byPrefix(String input) {
        // Must be longer than the input, and start with it.
        return s -> s.length() > input.length() && startsWithIgnoreCase(s, input);
    }

    private static boolean startsWithIgnoreCase(String whole, String prefix) {
        return whole.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    private SuggestionHelper() {
    }
}
