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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.enginehub.piston.CommandParseResult;
import org.enginehub.piston.converter.ArgumentConverterAccess;
import org.enginehub.piston.part.ArgAcceptingCommandFlag;
import org.enginehub.piston.part.ArgAcceptingCommandPart;
import org.enginehub.piston.part.CommandFlag;
import org.enginehub.piston.part.CommandPart;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;

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
        return ImmutableSet.copyOf(getSuggestionStream(args, parseResult).iterator());
    }

    private Stream<Suggestion> getSuggestionStream(List<String> args, CommandParseResult parseResult) {
        String last = Iterables.getLast(args, "");
        if (last.startsWith("-")) {
            // complete flags if we have any

            // check if the last flag in `last` is an arg-flag
            Optional<Stream<String>> argSuggestions = maybeSuggestArgFlag(last, "", parseResult);
            if (argSuggestions.isPresent()) {
                return argSuggestions.get()
                    .map(asSuggestion(args.size()));
            }
            Set<CommandFlag> flags = unmatchedFlags(parseResult);
            if (!flags.isEmpty()) {
                return suggestFlags(last, flags)
                    .map(asSuggestion(args.size() - 1));
            }
        }
        if (args.size() == parseResult.getBoundArguments().size()) {
            // all provided arguments are valid
            // suggest on empty for next argument
            return suggestUnmatchedArguments("", parseResult)
                .map(asSuggestion(args.size()));
        }
        if (args.size() > 1) {
            String secondToLast = args.get(args.size() - 2);
            if (secondToLast.startsWith("-")) {
                // this special case means we might be matching an arg-flag
                Optional<Stream<String>> argSuggestions =
                    maybeSuggestArgFlag(secondToLast, last, parseResult);
                if (argSuggestions.isPresent()) {
                    return argSuggestions.get()
                        .map(asSuggestion(args.size() - 1));
                }
            }
        }
        return suggestUnmatchedArguments(last, parseResult)
            .map(asSuggestion(args.size() - 1));
    }

    private Optional<Stream<String>> maybeSuggestArgFlag(String flags, String input, CommandParseResult parseResult) {
        if (flags.length() > 1) {
            char lastFlag = flags.charAt(flags.length() - 1);
            return parseResult.getPrimaryCommand().getParts()
                .stream()
                .filter(ArgAcceptingCommandFlag.class::isInstance)
                .map(ArgAcceptingCommandFlag.class::cast)
                .filter(f -> f.getName() == lastFlag)
                .findAny()
                .map(matchingArgLast ->
                    suggestFromParts(input, ImmutableSet.of(matchingArgLast), parseResult)
                );
        }
        return Optional.empty();
    }

    private Function<String, Suggestion> asSuggestion(int replacing) {
        return suggestion ->
            Suggestion.builder().suggestion(suggestion).replacedArgument(replacing).build();
    }

    private Stream<String> suggestFlags(String input, Set<CommandFlag> flags) {
        return flags.stream().map(flag -> input + flag.getName());
    }

    private Stream<String> suggestUnmatchedArguments(String input, CommandParseResult parseResult) {
        ImmutableList.Builder<ArgAcceptingCommandPart> parts = ImmutableList.builder();
        ImmutableSet<CommandPart> usedParts = ImmutableSet.copyOf(
            parseResult.getBoundArguments().stream()
                .flatMap(a -> a.getParts().stream())
                .iterator());
        for (CommandPart part : parseResult.getPrimaryCommand().getParts()) {
            if (part instanceof CommandFlag || usedParts.contains(part)) {
                continue;
            }
            checkState(part instanceof ArgAcceptingCommandPart, "%s does not accept arguments",
                part.getTextRepresentation());
            parts.add((ArgAcceptingCommandPart) part);
            if (part.isRequired()) {
                break;
            }
        }
        return suggestFromParts(input, parts.build(), parseResult);
    }

    private Set<CommandFlag> unmatchedFlags(CommandParseResult result) {
        Set<CommandPart> usedParts = result.getBoundArguments().stream()
            .flatMap(a -> a.getParts().stream())
            .collect(Collectors.toSet());
        return result.getPrimaryCommand().getParts().stream()
            .filter(CommandFlag.class::isInstance)
            .map(CommandFlag.class::cast)
            .filter(flag -> !usedParts.contains(flag))
            .collect(Collectors.toSet());
    }

    private Stream<String> suggestFromParts(String input,
                                            Collection<ArgAcceptingCommandPart> parts,
                                            CommandParseResult parseResult) {
        ArgumentConverterAccess converters = parseResult.getParameters().getConverters();
        return parts.stream()
            .filter(part -> part.getTypes().size() > 0)
            .flatMap(part -> part.getTypes().stream())
            .map(key -> converters.getConverter(key)
                .orElseThrow(() -> new IllegalStateException("No converter for type " + key)))
            .flatMap(converter -> converter.getSuggestions(input).stream());
    }
}
