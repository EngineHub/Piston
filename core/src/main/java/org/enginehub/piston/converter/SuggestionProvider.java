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

import org.enginehub.piston.inject.InjectedValueAccess;

import java.util.List;

/**
 * Represents an object than can provide suggestions, given an input and context.
 */
@FunctionalInterface
public interface SuggestionProvider {

    /**
     * Given {@code input} as the current input, provide some suggestions for the user.
     *
     * @param input the user's current input
     * @param context the context for the current command
     * @return suggestions for the user
     */
    List<String> getSuggestions(String input, InjectedValueAccess context);

}
