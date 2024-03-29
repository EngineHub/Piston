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

package org.enginehub.piston.suggestion;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.enginehub.piston.ArgBinding;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandParseResult;
import org.enginehub.piston.converter.ArgumentConverterAccess;
import org.enginehub.piston.part.ArgAcceptingCommandFlag;
import org.enginehub.piston.part.ArgAcceptingCommandPart;
import org.enginehub.piston.part.CommandFlag;
import org.enginehub.piston.part.CommandPart;
import org.enginehub.piston.part.SubCommandPart;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.enginehub.piston.converter.SuggestionHelper.byPrefix;
import static org.enginehub.piston.util.StreamHelper.cast;

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
        // validate that we only have one invalid argument
        if (args.size() - parseResult.getBoundArguments().size() > 1) {
            // too many -- return no suggestions, to hint that the user should back up
            return Stream.of();
        }
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
        if (args.size() == parseResult.getBoundArguments().size() && isLastExactMatch(parseResult.getBoundArguments())) {
            // all provided arguments are valid exact matches
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

    private boolean isLastExactMatch(ImmutableList<ArgBinding> argBindings) {
        if (argBindings.isEmpty()) {
            return true;
        }
        ArgBinding last = Iterables.getLast(argBindings);
        for (CommandPart commandPart : last.getParts()) {
            if (!last.isExactMatch(commandPart)) {
                return false;
            }
        }
        return true;
    }

    private Optional<Stream<String>> maybeSuggestArgFlag(String flags, String input, CommandParseResult parseResult) {
        if (flags.length() > 1) {
            char lastFlag = flags.charAt(flags.length() - 1);
            return cast(parseResult.getPrimaryCommand().getParts().stream(), ArgAcceptingCommandFlag.class)
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
        ImmutableList.Builder<CommandPart> parts = ImmutableList.builder();
        ImmutableSet<CommandPart> usedExactParts = ImmutableSet.copyOf(
            parseResult.getBoundArguments().stream()
                .flatMap(a -> a.getParts().stream().filter(a::isExactMatch))
                .iterator());
        for (CommandPart part : parseResult.getPrimaryCommand().getParts()) {
            if (part instanceof CommandFlag) {
                continue;
            }
            if (usedExactParts.contains(part)) {
                // also reset parts, we won't match prior to this either
                parts = ImmutableList.builder();
                continue;
            }
            parts.add(part);
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
        return cast(result.getPrimaryCommand().getParts().stream(), CommandFlag.class)
            .filter(flag -> !usedParts.contains(flag))
            .collect(Collectors.toSet());
    }

    private Stream<String> suggestFromParts(String input,
                                            Collection<CommandPart> parts,
                                            CommandParseResult parseResult) {
        ArgumentConverterAccess converters = parseResult.getParameters().getConverters();
        Predicate<String> nameFilter = byPrefix(input);
        return Stream.concat(
            cast(parts.stream(), ArgAcceptingCommandPart.class)
                .filter(part -> part.getTypes().size() > 0)
                .flatMap(part -> part.getTypes().stream())
                .map(key -> converters.getConverter(key)
                    .orElseThrow(() -> new IllegalStateException("No converter for type " + key)))
                .flatMap(converter -> converter.getSuggestions(input, parseResult.getParameters()).stream()),
            cast(parts.stream(), SubCommandPart.class)
                .flatMap(part -> part.getCommands().stream())
                .filter(c -> nameFilter.test(c.getName()) && c.getCondition().satisfied(parseResult.getParameters()))
                .map(Command::getName)
        );
    }
}
