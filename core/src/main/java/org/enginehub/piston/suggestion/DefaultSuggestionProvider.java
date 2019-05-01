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
import java.util.stream.Stream;

/**
 * Default provider for suggestions. Asks the argument converters for the given types
 * what suggestions should be provided.
 */
public class DefaultSuggestionProvider implements SuggestionProvider {

    private static final SuggestionProvider INSTANCE = new DefaultSuggestionProvider();

    public static SuggestionProvider getInstance() {
        return INSTANCE;
    }

    private DefaultSuggestionProvider() {
    }

    @Override
    public ImmutableSet<Suggestion> provideSuggestions(List<String> args, CommandParseResult parseResult) {
        Stream<String> suggestions;
        if (args.size() == parseResult.getBoundArguments().size()) {
            // all provided arguments are valid
            // consult the next argument converters for suggestions
            suggestions = suggestNewArgument(parseResult);
        } else {
            // filter arguments instead
            suggestions = suggestExistingArgument(args.get(args.size() - 1), parseResult);
        }
        return ImmutableSet.copyOf(
            suggestions
                .map(s -> Suggestion.builder()
                    .suggestion(s)
                    .replacedArgument(args.size())
                    .build())
                .iterator());
    }

    private Stream<String> suggestNewArgument(CommandParseResult parseResult) {
        return null;
    }

    private Stream<String> suggestExistingArgument(String input, CommandParseResult parseResult) {
        return null;
    }
}
