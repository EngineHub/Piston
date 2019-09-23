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
import org.enginehub.piston.config.ColorConfig;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ArgumentConverterAccess;
import org.enginehub.piston.converter.FailedConversion;
import org.enginehub.piston.exception.ConditionFailedException;
import org.enginehub.piston.exception.ConversionFailedException;
import org.enginehub.piston.exception.NoSuchFlagException;
import org.enginehub.piston.exception.UsageException;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.part.ArgAcceptingCommandFlag;
import org.enginehub.piston.part.ArgAcceptingCommandPart;
import org.enginehub.piston.part.ArgConsumingCommandPart;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.CommandFlag;
import org.enginehub.piston.part.CommandPart;
import org.enginehub.piston.part.NoArgCommandFlag;
import org.enginehub.piston.part.SubCommandPart;
import org.enginehub.piston.util.ComponentHelper;
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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

class CommandParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandParser.class);

    private static final ThreadLocal<String> PARSE_ID = new ThreadLocal<>();

    private static void log(String message, Object... args) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[" + PARSE_ID.get() + "]: " + message, args);
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
        final ListIterator<ArgConsumingCommandPart> partIter;
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
    @Nullable
    private CommandParseResult result;
    private boolean justUnconsumed;

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

    private void buildParseResult() {
        checkState(result == null, "Multiple calls to build final result");
        if (argBindings.build().size() > 0 && argIter.hasPrevious()) {
            bindArgument();
        }
        fillInDefaults();
        result = parseResult
            .parameters(parameters
                .metadata(metadata)
                .injectedValues(context)
                .converters(converters)
                .build())
            .build();
    }

    private CommandParseResult getResult() {
        return checkNotNull(result, "Not finished parsing");
    }

    private UsageException usageException(Component message) {
        buildParseResult();
        return new UsageException(message, getResult());
    }

    private UsageException notEnoughArgumentsException() {
        return usageException(TextComponent.of("Not enough arguments."));
    }

    private UsageException tooManyArgumentsException() {
        return usageException(TextComponent.of("Too many arguments."));
    }

    private ConversionFailedException conversionFailedException(ArgAcceptingCommandPart nextArg, String token) {
        // TODO: Make this print all converters
        ArgumentConverter<?> converter = nextArg.getTypes().stream()
            .map(k -> converters.getConverter(k).orElse(null))
            .filter(Objects::nonNull)
            .findFirst().orElseThrow(IllegalStateException::new);
        buildParseResult();
        return new ConversionFailedException(getResult(),
            nextArg.getTextRepresentation(),
            converter,
            (FailedConversion<?>) converter.convert(token, context));
    }

    private ConditionFailedException conditionFailed() {
        buildParseResult();
        return new ConditionFailedException(getResult().getExecutionPath());
    }

    private boolean testCondition(Command.Condition condition) {
        return condition.satisfied(context);
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
        checkState(hasNextArgument(),
            "No next argument present, this call should be guarded with hasNextArgument");
        // don't try to re-bind the last argument if we have already done it,
        // and reversed with unconsumeArgument
        if (argIter.hasPrevious() && !justUnconsumed) {
            bindArgument();
        }
        justUnconsumed = false;
        String next = argIter.next();
        argBindings = ImmutableSet.builder();
        return next;
    }

    private void bindArgument() {
        ImmutableSet<CommandPart> binding = argBindings.build();
        checkState(!binding.isEmpty() || currentArgument().equals("--"),
            "Argument never bound: %s", currentArgument());
        parseResult.addArgument(ArgBindingImpl.builder()
            .input(currentArgument())
            .parts(binding)
            .build());
    }

    private void unconsumeArgument() {
        checkState(argBindings.build().isEmpty(),
            "Argument already bound: %s", currentArgument());
        checkState(argIter.hasPrevious(),
            "Trying to unconsume nothing");
        argIter.previous();
        justUnconsumed = true;
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

    private ArgConsumingCommandPart nextPart() {
        checkState(hasNextPart(),
            "No next part, this call should be guarded with hasNextPart()");
        return perCommandDetails().partIter.next();
    }

    private void unconsumePart() {
        ListIterator<ArgConsumingCommandPart> partIter = perCommandDetails().partIter;
        checkState(partIter.hasPrevious(),
            "Trying to unconsume nothing");
        partIter.previous();
    }

    private void bind(CommandPart part) {
        argBindings.add(part);
    }

    private void switchToCommand(Command subCommand) {
        if (perCommandDetails != null) {
            fillInDefaults();
        }
        parseResult.addCommand(subCommand);
        perCommandDetails = new PerCommandDetails(commandInfoCache.getInfo(subCommand));

        if (!testCondition(subCommand.getCondition())) {
            throw conditionFailed();
        }
    }

    private void fillInDefaults() {
        for (ArgAcceptingCommandPart part : perCommandDetails().defaultsNeeded) {
            addValueFull(part, v -> v.values(part.getDefaults()));
        }
    }

    private void finalizeCommand() {
        PerCommandDetails details = perCommandDetails();
        if (details.remainingRequiredParts > 0) {
            Iterator<ArgConsumingCommandPart> requiredIter = Iterators.filter(details.partIter, CommandPart::isRequired);
            if (requiredIter.hasNext()) {
                ArgConsumingCommandPart missing = requiredIter.next();
                if (missing instanceof CommandArgument) {
                    throw usageException(TextComponent.builder("Missing argument for ")
                        .append(missing.getTextRepresentation())
                        .append(TextComponent.of("."))
                        .build());
                } else {
                    checkState(missing instanceof SubCommandPart,
                        "Unknown part interface: %s", missing.getClass());
                    throw usageException(TextComponent.of("No sub-command provided. Options: "
                        + ((SubCommandPart) missing).getCommands().stream()
                        .distinct()
                        .map(Command::getName)
                        .collect(Collectors.joining(", "))));
                }
            }
        }
    }

    CommandParseResult parse() {
        logParseStart();
        while (hasNextArgument()) {
            String token = nextArgument();
            log("Consuming argument `{}`", token);
            PerCommandDetails details = perCommandDetails();

            if (isFlag(token)) {
                if (token.equals("--")) {
                    log("Encountered `--`, turning off flag matching.");
                    details.canMatchFlags = false;
                    continue;
                }

                parseFlags(token.substring(1));
                continue;
            }

            if (!parseRegularArgument(token)) {
                // Hit end of parts, this cannot be parsed
                if (lastFailedOptional != null) {
                    // fail on type-conversion to this instead
                    throw conversionFailedException(lastFailedOptional, token);
                }
                throw tooManyArgumentsException();
            }
        }
        log("Finished looking at arguments. Finalizing command.");
        finalizeCommand();
        buildParseResult();
        return getResult();
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

    private boolean parseSubCommand(SubCommandPart part, String token) {
        CommandInfo commandInfo = perCommandDetails().commandInfo;
        ImmutableMap<String, Command> subCommands = commandInfo.subCommandTable.row(part);
        Command sub = subCommands.get(token);
        if (sub == null) {
            return false;
        }
        bind(part);
        if (part.isRequired()) {
            perCommandDetails().remainingRequiredParts--;
        }
        switchToCommand(sub);
        return true;
    }

    private TextComponent invalidSubCommandMessage(String token, ImmutableMap<String, Command> subCommands) {
        return TextComponent.builder()
            .append("Invalid sub-command '")
            .append(ColorConfig.mainText().wrap(token))
            .append("'. Options: ")
            .append(subCommands.values().stream().distinct()
                .map(Command::getName)
                .map(ColorConfig.mainText()::wrap)
                .collect(ComponentHelper.joiningTexts(
                    TextComponent.empty(),
                    TextComponent.of(", "),
                    TextComponent.empty()
                )))
            .build();
    }

    private boolean parseRegularArgument(String token) {
        PerCommandDetails details = perCommandDetails();
        if (!hasNextPart()) {
            log("parseRegularArgument: no arguments to attempt matching");
        }
        CommandArgument lastFailedOptionalLocal = null;
        while (hasNextPart()) {
            ArgConsumingCommandPart nextArg = nextPart();
            if (nextArg instanceof SubCommandPart) {
                SubCommandPart subCommandPart = (SubCommandPart) nextArg;
                if (parseSubCommand(subCommandPart, token)) {
                    return true;
                }
                if (nextArg.isRequired()) {
                    throw usageException(
                        invalidSubCommandMessage(
                            token,
                            details.commandInfo.subCommandTable.row(subCommandPart)
                        ));
                }
                continue;
            }
            checkState(nextArg instanceof CommandArgument,
                "Unknown part interface: %s", nextArg.getClass());
            CommandArgument argPart = (CommandArgument) nextArg;
            if (nextArg.isRequired()) {
                // good, we can just satisfy it
                if (!isAcceptedByTypeParsers(argPart, token)) {
                    throw conversionFailedException(argPart, token);
                }
                details.remainingRequiredParts--;
                addValueFull(nextArg, v -> v.values(consumeArguments(argPart, token)));
                return true;
            } else {
                if (details.commandInfo.subCommandTable.isEmpty()) {
                    // No sub-commands -- we can fill optionals based on remaining argument count
                    int remainingArguments = remainingNonFlagArguments();
                    int diff = remainingArguments - details.remainingRequiredParts;
                    if (diff < 0) {
                        throw notEnoughArgumentsException();
                    } else if (diff == 0) {
                        // do not fill -- save for a required argument
                        continue;
                    }
                    // may fill this if it matches, fall to below
                }
                if (isAcceptedByTypeParsers(argPart, token)) {
                    details.defaultsNeeded.remove(nextArg);
                    addValueFull(nextArg, v -> v.values(consumeArguments(argPart, token)));
                    return true;
                }
                // store it in case no required arguments match
                lastFailedOptionalLocal = argPart;
            }
        }
        lastFailedOptional = lastFailedOptionalLocal;
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
                buildParseResult();
                throw new NoSuchFlagException(getResult(), c);
            }
            if (flag instanceof ArgAcceptingCommandFlag) {
                if (i + 1 < flags.length()) {
                    // Only allow argument-flags at the end of flag-combos.
                    throw usageException(TextComponent.of("Argument-accepting flags must be " +
                        "at the end of combined flag groups."));
                }
                bind(flag);
                if (!hasNextArgument()) {
                    log("parseFlags: [-{}] skipping argument for arg-accepting flag, no argument available",
                        flag.getName());
                    break;
                }
                String nextToken = nextArgument();
                ArgAcceptingCommandFlag argPart = (ArgAcceptingCommandFlag) flag;
                if (!isAcceptedByTypeParsers(argPart, nextToken)) {
                    log("parseFlags: [-{}] skipping argument for arg-accepting flag, not accepted by type parsers",
                        flag.getName());
                    unconsumeArgument();
                    break;
                }
                addValueFull(flag, v -> v.value(nextToken));
                bind(flag);
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
            .commandContextSupplier(this::getResult)
            .partContext(part)
            .injectedValues(context)
            .manager(converters)
            .build());
    }

}
