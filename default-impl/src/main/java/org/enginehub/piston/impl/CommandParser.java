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

package org.enginehub.piston.impl;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandMetadata;
import org.enginehub.piston.CommandParseResult;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ArgumentConverterAccess;
import org.enginehub.piston.converter.FailedConversion;
import org.enginehub.piston.exception.ConversionFailedException;
import org.enginehub.piston.exception.NoSuchFlagException;
import org.enginehub.piston.exception.UsageException;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.part.ArgAcceptingCommandFlag;
import org.enginehub.piston.part.ArgAcceptingCommandPart;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.CommandFlag;
import org.enginehub.piston.part.CommandPart;
import org.enginehub.piston.part.NoArgCommandFlag;
import org.enginehub.piston.part.SubCommandPart;
import org.enginehub.piston.util.TextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

class CommandParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandParser.class);

    private static final ThreadLocal<String> PARSE_ID = new ThreadLocal<>();

    private static void log(String message, Object... args) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[" + PARSE_ID.get() + "]: " + message, (Object[]) args);
        }
    }

    private static String newId() {
        long rng = ThreadLocalRandom.current().nextLong(0, Long.MAX_VALUE);
        return Strings.padStart(Long.toString(rng, 36), 13, '0');
    }

    private static void logParseStart() {
        PARSE_ID.set(newId());
        log("started parsing");
    }

    private static final class PerCommandDetails {

        final CommandInfo commandInfo;
        final Set<ArgAcceptingCommandPart> defaultsNeeded;
        final ListIterator<CommandArgument> partIter;
        boolean canMatchFlags = true;
        int remainingRequiredParts;

        private PerCommandDetails(CommandInfo commandInfo) {
            this.commandInfo = commandInfo;
            this.defaultsNeeded = new HashSet<>(commandInfo.defaultProvided);
            this.partIter = commandInfo.arguments.listIterator();
            this.remainingRequiredParts = commandInfo.requiredParts;
        }
    }

    private final ArgumentConverterAccess converters;
    private final CommandMetadata metadata;
    private final CommandParseResultImpl.Builder parseResult = CommandParseResultImpl.builder();
    private final CommandParametersImpl.Builder parameters = CommandParametersImpl.builder();
    private final CommandInfoCache commandInfoCache;
    private final ImmutableList<String> arguments;
    private final ListIterator<String> argIter;
    private final InjectedValueAccess context;
    private ImmutableSet.Builder<CommandPart> argBindings = ImmutableSet.builder();
    @Nullable
    private PerCommandDetails perCommandDetails;
    @Nullable
    private CommandArgument lastFailedOptional;

    CommandParser(ArgumentConverterAccess converters, CommandInfoCache commandInfoCache, Command initial,
                  CommandMetadata metadata, InjectedValueAccess context) {
        this.commandInfoCache = commandInfoCache;
        this.converters = converters;
        this.metadata = metadata;
        this.arguments = metadata.getArguments();
        this.argIter = this.arguments.listIterator();
        this.context = context;
        switchToCommand(initial);
    }

    private CommandParseResult buildParseResult() {
        if (argBindings.build().size() > 0 && argIter.hasPrevious()) {
            bindArgument();
        }
        fillInDefaults();
        return parseResult
            .parameters(parameters
                .metadata(metadata)
                .injectedValues(context)
                .converters(converters)
                .build())
            .build();
    }

    private UsageException usageException(Component message) {
        return new UsageException(message, buildParseResult());
    }

    private UsageException notEnoughArgumentsException() {
        return usageException(TextComponent.of("Not enough arguments."));
    }

    private UsageException tooManyArgumentsException() {
        return usageException(TextComponent.of("Too many arguments."));
    }

    private ConversionFailedException conversionFailedException(CommandArgument nextArg, String token) {
        // TODO: Make this print all converters
        ArgumentConverter<?> converter = nextArg.getTypes().stream()
            .map(k -> converters.getConverter(k).orElse(null))
            .filter(Objects::nonNull)
            .findFirst().orElseThrow(IllegalStateException::new);
        return new ConversionFailedException(buildParseResult(),
            nextArg.getTextRepresentation(),
            converter,
            (FailedConversion<?>) converter.convert(token, context));
    }

    private PerCommandDetails perCommandDetails() {
        return requireNonNull(perCommandDetails);
    }

    private String currentArgument() {
        int argumentIndex = argIter.previousIndex();
        if (argumentIndex < 0) {
            throw new IllegalStateException("No argument has been asked for yet");
        }
        return arguments.get(argumentIndex);
    }

    private boolean hasNextArgument() {
        return argIter.hasNext();
    }

    private String nextArgument() {
        if (!hasNextArgument()) {
            throw notEnoughArgumentsException();
        }
        if (argIter.hasPrevious()) {
            bindArgument();
        }
        String next = argIter.next();
        argBindings = ImmutableSet.builder();
        return next;
    }

    private void bindArgument() {
        ImmutableSet<CommandPart> binding = argBindings.build();
        if (binding.isEmpty() && !currentArgument().equals("--")) {
            throw new IllegalStateException("Argument never bound: " + currentArgument());
        }
        parseResult.addArgument(ArgBindingImpl.builder()
            .input(currentArgument())
            .parts(binding)
            .build());
    }

    private void unconsumeArgument() {
        if (!argBindings.build().isEmpty()) {
            throw new IllegalStateException("Argument already bound: " + currentArgument());
        }
        if (!argIter.hasPrevious()) {
            throw new IllegalStateException("Trying to unconsume nothing");
        }
        argIter.previous();
    }

    private int remainingNonFlagArguments() {
        return (int) arguments.stream()
            .skip(argIter.previousIndex())
            .filter(s -> !isFlag(s))
            .count();
    }

    private boolean hasNextPart() {
        return perCommandDetails().partIter.hasNext();
    }

    private CommandArgument nextPart() {
        if (!hasNextPart()) {
            throw usageException(TextComponent.of("Too many arguments."));
        }
        return perCommandDetails().partIter.next();
    }

    private void bind(CommandPart part) {
        argBindings.add(part);
    }

    private void switchToCommand(Command subCommand) {
        if (perCommandDetails != null) {
            finalizeCommand();
            fillInDefaults();
        }
        parseResult.addCommand(subCommand);
        perCommandDetails = new PerCommandDetails(commandInfoCache.getInfo(subCommand));
    }

    private void fillInDefaults() {
        for (ArgAcceptingCommandPart part : perCommandDetails().defaultsNeeded) {
            addValueFull(part, v -> v.values(part.getDefaults()));
        }
    }

    private void finalizeCommand() {
        PerCommandDetails details = perCommandDetails();
        if (details.remainingRequiredParts > 0) {
            Iterator<CommandArgument> requiredIter = Iterators.filter(details.partIter, CommandPart::isRequired);
            if (requiredIter.hasNext()) {
                CommandArgument missing = requiredIter.next();
                throw usageException(TextComponent.builder("Missing argument for ")
                    .append(missing.getTextRepresentation())
                    .append(TextComponent.of("."))
                    .build());
            } else {
                // sanity check for remaining required parts
                checkState(details.commandInfo.subCommandPart.filter(SubCommandPart::isRequired).isPresent());
                throw usageException(TextComponent.of("No sub-command provided. Options: "
                    + details.commandInfo.subCommands.values().stream()
                    .distinct()
                    .map(Command::getName)
                    .collect(Collectors.joining(", "))));
            }
        }
    }

    CommandParseResult parse() {
        logParseStart();
        while (hasNextArgument()) {
            String token = nextArgument();
            log("Consuming argument `{}`", token);

            if (isFlag(token)) {
                if (token.equals("--")) {
                    log("Encountered `--`, turning off flag matching.");
                    perCommandDetails().canMatchFlags = false;
                    continue;
                }

                parseFlags(token.substring(1));
                continue;
            }

            if (!parseRegularArgument(token)) {
                log("Failed to parse {} as regular argument, attempting sub-command.", token);
                // Hit end of parts. Maybe this belongs to sub-commands?
                if (!parseSubCommand(token)) {
                    if (lastFailedOptional != null) {
                        // fail on type-conversion to this instead
                        throw conversionFailedException(lastFailedOptional, token);
                    }
                    throw tooManyArgumentsException();
                }
            }
        }
        log("Finished looking at arguments. Finalizing command.");
        finalizeCommand();
        return buildParseResult();
    }

    private boolean isFlag(String token) {
        if (token.length() <= 1 || !perCommandDetails().canMatchFlags) {
            return false;
        }
        if (!token.startsWith("-")) {
            return false;
        }
        if (token.equals("--")) {
            return true;
        }

        return token.codePoints()
            .skip(1)
            .allMatch(cp -> perCommandDetails().commandInfo.flags.containsKey((char) cp));
    }

    private boolean parseSubCommand(String token) {
        CommandInfo commandInfo = perCommandDetails().commandInfo;
        if (!commandInfo.subCommandPart.isPresent()) {
            return false;
        }
        ImmutableMap<String, Command> subCommands = commandInfo.subCommands;
        Command sub = subCommands.get(token);
        if (sub == null) {
            throw usageException(TextComponent.of("Invalid sub-command. Options: "
                + subCommands.values().stream()
                .distinct()
                .map(Command::getName)
                .collect(Collectors.joining(", "))));
        }
        SubCommandPart part = commandInfo.subCommandPart.get();
        bind(part);
        if (part.isRequired()) {
            perCommandDetails().remainingRequiredParts--;
        }
        switchToCommand(sub);
        return true;
    }

    private boolean parseRegularArgument(String token) {
        PerCommandDetails details = perCommandDetails();
        if (!hasNextPart()) {
            log("parseRegularArgument: no arguments to attempt matching");
        }
        while (hasNextPart()) {
            CommandArgument nextArg = nextPart();
            String name = TextHelper.reduceToText(nextArg.getName());
            log("parseRegularArgument: [{}] test for matching", name);
            if (nextArg.isRequired()) {
                // good, we can just satisfy it
                if (!isAcceptedByTypeParsers(nextArg, token)) {
                    throw conversionFailedException(nextArg, token);
                }
                details.remainingRequiredParts--;
                addValueFull(nextArg, v -> v.values(consumeArguments(nextArg, token)));
                return true;
            }
            log("parseRegularArgument: [{}] not required, trying optional tests", name);
            if (details.commandInfo.subCommands.isEmpty()) {
                log("parseRegularArgument: [{}] using remaining-required test", name);
                // No sub-commands -- we can fill optionals based on remaining argument count
                int remainingArguments = remainingNonFlagArguments();
                int diff = remainingArguments - details.remainingRequiredParts;
                if (diff < 0) {
                    throw notEnoughArgumentsException();
                } else if (diff == 0) {
                    // do not fill -- save for a required argument
                    log("parseRegularArgument: [{}] remaining-required SOFT_FAIL:" +
                            " remaining={}, required={}",
                        name, remainingArguments, details.remainingRequiredParts);
                    continue;
                }
                log("parseRegularArgument: [{}] passed remaining-required test", name);
                // may fill this if it matches, fall to below
            }
            log("parseRegularArgument: [{}] using type-parser test", name);
            if (isAcceptedByTypeParsers(nextArg, token)) {
                log("parseRegularArgument: [{}] passed type-parser test", name);
                details.defaultsNeeded.remove(nextArg);
                addValueFull(nextArg, v -> v.values(consumeArguments(nextArg, token)));
                return true;
            }
            log("parseRegularArgument: [{}] type-parser SOFT_FAIL:" +
                " types={}", name, nextArg.getTypes());
            // store it in case no required arguments match
            lastFailedOptional = nextArg;
        }
        return false;
    }

    private ImmutableList<String> consumeArguments(CommandArgument nextArg, String first) {
        ImmutableList.Builder<String> result = ImmutableList.builder();
        bind(nextArg);
        result.add(first);
        if (nextArg.isVariable()) {
            while (hasNextArgument()) {
                String next = nextArgument();
                if (isAcceptedByTypeParsers(nextArg, next)) {
                    bind(nextArg);
                    result.add(next);
                } else {
                    unconsumeArgument();
                    break;
                }
            }
        }
        return result.build();
    }

    /**
     * Check if {@code part} has type converters attached, and if so, return
     * {@code true} iff any of them will convert {@code next}. If there are no
     * type converters, also return {@code true}.
     */
    private boolean isAcceptedByTypeParsers(ArgAcceptingCommandPart part,
                                            String next) {
        ImmutableSet<Key<?>> types = part.getTypes();
        if (types.isEmpty()) {
            return true;
        }

        return types.stream().anyMatch(type -> {
            Optional<? extends ArgumentConverter<?>> argumentConverter = converters.getConverter(type);
            if (!argumentConverter.isPresent()) {
                throw new IllegalStateException("No argument converter for " + type);
            }
            return argumentConverter.get().convert(next, context).isSuccessful();
        });
    }

    private void parseFlags(String flags) {
        for (int i = 0; i < flags.length(); i++) {
            char c = flags.charAt(i);
            CommandFlag flag = perCommandDetails().commandInfo.flags.get(c);
            if (flag == null) {
                throw new NoSuchFlagException(buildParseResult(), c);
            }
            if (flag instanceof ArgAcceptingCommandFlag) {
                if (i + 1 < flags.length()) {
                    // Only allow argument-flags at the end of flag-combos.
                    throw usageException(TextComponent.of("Argument-accepting flags must be " +
                        "at the end of combined flag groups."));
                }
                bind(flag);
                if (!hasNextArgument()) {
                    log("parseFlags: [-{}] skipping argument for arg-accepting flag",
                        flag.getName());
                    break;
                }
                addValueFull(flag, v -> v.value(nextArgument()));
                perCommandDetails().defaultsNeeded.remove(flag);
            } else {
                // Sanity-check. Real check is in `CommandInfo.from`.
                checkState(flag instanceof NoArgCommandFlag);
                bind(flag);
                parameters.addPresentPart(flag);
            }
        }
    }

    private void addValueFull(CommandPart part,
                              Consumer<CommandValueImpl.Builder> valueAdder) {
        parameters.addPresentPart(part);
        CommandValueImpl.Builder builder = CommandValueImpl.builder();
        valueAdder.accept(builder);
        parameters.addValue(part, builder
            .commandContextSupplier(this::buildParseResult)
            .partContext(part)
            .injectedValues(context)
            .manager(converters)
            .build());
    }

}
