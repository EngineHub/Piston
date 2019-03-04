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

package org.enginehub.piston.converter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Converts user input into an actual type. It can provide multiple
 * results per argument.
 *
 * @param <T> the type of the result
 */
public interface ArgumentConverter<T> {

    /**
     * Converts the argument input to a collection of argument values.
     *
     * @param argument the argument input to convert
     * @return the argument values
     */
    Collection<T> convert(String argument);

    /**
     * Describe the arguments that can be provided to this converter.
     *
     * <p>
     * This information is displayed to the user.
     * </p>
     *
     * @return a description of acceptable arguments
     */
    String describeAcceptableArguments();

    /**
     * Given {@code input} as the current input, provide some suggestions for the user.
     *
     * @param input the user's current input
     * @return suggestions for the user
     */
    default List<String> getSuggestions(String input) {
        return Collections.emptyList();
    }

}
