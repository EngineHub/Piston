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

package org.enginehub.piston.suggestion;

import com.google.common.collect.ImmutableSet;
import org.enginehub.piston.CommandParseResult;

import java.util.List;

/**
 * General suggestion provider.
 */
public interface SuggestionProvider {

    /**
     * Given args and the result of parsing them, provide suggestions for the next argument.
     *
     * <p>
     * Assume that the argument that needs completion is the last one, unless
     * {@code parseResult}'s bound argument count is equal to {@code args.size()},
     * in which case provide options for a new, empty, argument. The
     * {@linkplain Suggestion#getReplacedArgument() replaced argument} should be set to
     * {@code args.size()} in this case, to signify it replaces outside of the list.
     * </p>
     *
     * @param args the original arguments passed
     * @param parseResult the result of parsing the arguments
     * @return the suggestions
     */
    ImmutableSet<Suggestion> provideSuggestions(List<String> args, CommandParseResult parseResult);

}
