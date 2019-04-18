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

import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandMetadata;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ArgumentConverters;
import org.enginehub.piston.exception.CommandException;
import org.enginehub.piston.exception.CommandExecutionException;
import org.enginehub.piston.exception.ConditionFailedException;
import org.enginehub.piston.exception.NoSuchCommandException;
import org.enginehub.piston.exception.NoSuchFlagException;
import org.enginehub.piston.exception.UsageException;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.inject.MemoizingValueAccess;
import org.enginehub.piston.inject.MergedValueAccess;
import org.enginehub.piston.part.ArgAcceptingCommandFlag;
import org.enginehub.piston.part.ArgAcceptingCommandPart;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.CommandFlag;
import org.enginehub.piston.part.CommandPart;
import org.enginehub.piston.part.NoArgCommandFlag;
import org.enginehub.piston.part.SubCommandPart;
import org.enginehub.piston.util.ValueProvider;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class CommandManagerImpl implements CommandManager {

    private static CommandParseCache cacheCommand(Command command) {
        ImmutableList.Builder<CommandArgument> arguments = ImmutableList.builder();
        ImmutableList.Builder<ArgAcceptingCommandPart> defaultProvided = ImmutableList.builder();
        ImmutableMap.Builder<Character, CommandFlag> flags = ImmutableMap.builder();
        ImmutableMap.Builder<String, Command> subCommands = ImmutableMap.builder();
        boolean subCommandRequired = false;
        ImmutableList<CommandPart> parts = command.getParts();
        for (int i = 0; i < parts.size(); i++) {
            CommandPart part = parts.get(i);
            if (part instanceof ArgAcceptingCommandFlag || part instanceof NoArgCommandFlag) {
                CommandFlag flag = (CommandFlag) part;
                flags.put(flag.getName(), flag);
            } else if (part instanceof CommandArgument) {
                arguments.add((CommandArgument) part);
            } else if (part instanceof SubCommandPart) {
                checkState(i + 1 >= parts.size(),
                    "Sub-command must be last part.");
                for (Command cmd : ((SubCommandPart) part).getCommands()) {
                    subCommands.put(cmd.getName(), cmd);
                    for (String alias : cmd.getAliases()) {
                        subCommands.put(alias, cmd);
                    }
                }
                subCommandRequired = part.isRequired();
            } else {
                throw new IllegalStateException("Unknown part implementation " + part);
            }
            if (part instanceof ArgAcceptingCommandPart) {
                ArgAcceptingCommandPart argPart = (ArgAcceptingCommandPart) part;
                if (argPart.getDefaults().size() > 0) {
                    defaultProvided.add(argPart);
                }
            }
        }
        return new CommandParseCache(
            arguments.build(),
            defaultProvided.build(),
            flags.build(),
            subCommands.build(),
            subCommandRequired);
    }

    private static class CommandParseCache {
        final ImmutableList<CommandArgument> arguments;
        final ImmutableList<ArgAcceptingCommandPart> defaultProvided;
        final ImmutableMap<Character, CommandFlag> flags;
        final ImmutableMap<String, Command> subCommands;
        final boolean subCommandRequired;

        CommandParseCache(ImmutableList<CommandArgument> arguments,
                          ImmutableList<ArgAcceptingCommandPart> defaultProvided,
                          ImmutableMap<Character, CommandFlag> flags,
                          ImmutableMap<String, Command> subCommands,
                          boolean subCommandRequired) {
            this.arguments = arguments;
            this.defaultProvided = defaultProvided;
            this.flags = flags;
            this.subCommands = subCommands;
            this.subCommandRequired = subCommandRequired;
        }
    }

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<String, Command> commands = new HashMap<>();
    // Cache information like flags if we can, but let GC clear it if needed.
    private final LoadingCache<Command, CommandParseCache> commandCache = CacheBuilder.newBuilder()
        .softValues()
        .build(CacheLoader.from(CommandManagerImpl::cacheCommand));
    private final Map<Key<?>, ValueProvider<InjectedValueAccess, ?>> injectedValues = new HashMap<>();
    private final Map<Key<?>, ArgumentConverter<?>> converters = new HashMap<>();

    public CommandManagerImpl() {
        registerConverter(Key.of(String.class), ArgumentConverters.forString());
        for (Class<?> wrapperType : ImmutableList.of(
            Byte.class, Short.class, Integer.class, Long.class,
            Float.class, Double.class,
            Character.class, Boolean.class
        )) {
            // just forcing the generic to work
            @SuppressWarnings("unchecked")
            Class<Object> fake = (Class<Object>) wrapperType;
            registerConverter(Key.of(fake), ArgumentConverters.get(TypeToken.of(fake)));
        }
    }

    @Override
    public Command.Builder newCommand(String name) {
        return CommandImpl.builder(name);
    }

    @Override
    public void register(Command command) {
        // Run it through the cache for a validity check,
        // and so that we can cache many commands in high-memory situations.
        commandCache.getUnchecked(command);
        lock.writeLock().lock();
        try {
            registerIfAvailable(command.getName(), command);
            for (String alias : command.getAliases()) {
                registerIfAvailable(alias, command);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void registerIfAvailable(String name, Command command) {
        Command existing = commands.put(name, command);
        if (existing != null) {
            commands.put(name, existing);
            throw new IllegalArgumentException("A command is already registered under "
                + name + "; existing=" + existing + ",rejected=" + command);
        }
    }

    @Override
    public <T> void registerConverter(Key<T> key, ArgumentConverter<T> converter) {
        lock.writeLock().lock();
        try {
            converters.put(key, converter);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public <T> Optional<ArgumentConverter<T>> getConverter(Key<T> key) {
        @SuppressWarnings("unchecked")
        ArgumentConverter<T> converter = (ArgumentConverter<T>) getArgumentConverter(key);
        return Optional.ofNullable(converter);
    }

    @Nullable
    private <T> ArgumentConverter<?> getArgumentConverter(Key<T> key) {
        lock.readLock().lock();
        try {
            return converters.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public <T> void injectValue(Key<T> key, ValueProvider<InjectedValueAccess, T> supplier) {
        checkNotNull(key, "key cannot be null");
        checkNotNull(supplier, "supplier cannot be null");
        lock.writeLock().lock();
        try {
            injectedValues.put(key, supplier);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    // stored only by injectValue, with matching T, so this is safe
    @SuppressWarnings("unchecked")
    public <T> Optional<T> injectedValue(Key<T> key) {
        return (Optional<T>) injectedValues.get(key).value(this);
    }

    @Override
    public Stream<Command> getAllCommands() {
        ImmutableList<Command> allCommands;
        lock.readLock().lock();
        try {
            allCommands = ImmutableList.copyOf(commands.values());
        } finally {
            lock.readLock().unlock();
        }
        return allCommands.stream().distinct();
    }

    @Override
    public Optional<Command> getCommand(String name) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(commands.get(name));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int execute(InjectedValueAccess context, List<String> args) {
        lock.readLock().lock();
        try {
            String name = args.get(0);
            Command command = commands.get(name);
            if (command == null) {
                throw new NoSuchCommandException(name);
            }
            // order given context first, then resolve manager values
            // cache all values
            InjectedValueAccess fullContext = MemoizingValueAccess.wrap(
                MergedValueAccess.of(context, this)
            );
            return executeSubCommand(name, command, fullContext, args.subList(1, args.size()));
        } finally {
            lock.readLock().unlock();
        }
    }

    private int executeSubCommand(String calledName, Command command,
                                  InjectedValueAccess context, List<String> args) {
        CommandParametersImpl.Builder parameters = CommandParametersImpl.builder()
            .injectedValues(context);
        CommandParseCache parseCache = commandCache.getUnchecked(command);

        boolean flagsEnabled = true;
        Set<ArgAcceptingCommandPart> defaultsNeeded = new HashSet<>(parseCache.defaultProvided);
        Iterator<CommandArgument> partIter = parseCache.arguments.iterator();
        Iterator<String> argIter = args.iterator();
        while (argIter.hasNext()) {
            String next = argIter.next();

            // Handle flags:
            if (next.startsWith("-") && flagsEnabled) {
                if (next.equals("--")) {
                    // Special option to stop flag handling.
                    flagsEnabled = false;
                } else {
                    // Pick out individual flags from the long-option form.
                    consumeFlags(command, context, parameters, parseCache, defaultsNeeded, argIter, next);
                }
                continue;
            }

            // Otherwise, eat it as the current argument.
            if (!partIter.hasNext()) {
                // we may still consume as a sub-command
                if (parseCache.subCommands.isEmpty()) {
                    // but not in this case
                    break;
                }
                Command sub = parseCache.subCommands.get(next);
                if (sub == null) {
                    throw new UsageException("Bad sub-command. Acceptable commands: "
                        + Joiner.on(", ").join(parseCache.subCommands.keySet()), command);
                }
                return executeSubCommand(next, sub, context, ImmutableList.copyOf(argIter));
            }
            CommandArgument nextPart = partIter.next();
            addValueFull(parameters, command, nextPart, context, v -> v.value(next));
            defaultsNeeded.remove(nextPart);
        }

        // Handle error conditions.
        boolean moreParts = partIter.hasNext() && partIter.next().isRequired();
        // The sub-command is only handled on empty-parts.
        // If we made it here, we ran out of arguments before calling into it.
        if (moreParts || parseCache.subCommandRequired) {
            checkState(!argIter.hasNext(), "Should not have more arguments to analyze.");
            throw new UsageException("Not enough arguments", command);
        }

        if (argIter.hasNext()) {
            checkState(!partIter.hasNext(), "Should not have more parts to analyze.");
            throw new UsageException("Too many arguments", command);
        }

        for (ArgAcceptingCommandPart part : defaultsNeeded) {
            addValueFull(parameters, command, part, context, v -> v.values(part.getDefaults()));
        }

        // Run the command action.
        try {
            CommandMetadata metadata = CommandMetadataImpl.builder()
                .calledName(calledName)
                .arguments(args)
                .build();
            CommandParametersImpl builtParams = parameters.metadata(metadata).build();
            if (!command.getCondition().satisfied(builtParams)) {
                throw new ConditionFailedException(command);
            }

            return command.getAction().run(builtParams);
        } catch (CommandException e) {
            throw e;
        } catch (Exception e) {
            throw new CommandExecutionException(e, command);
        }
    }

    private void consumeFlags(Command command,
                              InjectedValueAccess injectedValues,
                              CommandParametersImpl.Builder parameters,
                              CommandParseCache parseCache,
                              Set<ArgAcceptingCommandPart> defaultsNeeded,
                              Iterator<String> argIter, String next) {
        char[] flagArray = new char[next.length() - 1];
        next.getChars(1, next.length(), flagArray, 0);
        for (int i = 0; i < flagArray.length; i++) {
            char c = flagArray[i];
            CommandFlag flag = parseCache.flags.get(c);
            if (flag == null) {
                throw new NoSuchFlagException(command, c);
            }
            if (flag instanceof ArgAcceptingCommandFlag) {
                if (i + 1 < flagArray.length) {
                    // Only allow argument-flags at the end of flag-combos.
                    throw new UsageException("Argument-accepting flags must be " +
                        "at the end of combined flag groups.", command);
                }
                if (!argIter.hasNext()) {
                    break;
                }
                addValueFull(parameters, command, flag, injectedValues, v -> v.value(argIter.next()));
                defaultsNeeded.remove(flag);
            } else {
                // Sanity-check. Real check is in `cacheCommand`.
                checkState(flag instanceof NoArgCommandFlag);
                parameters.addPresentPart(flag);
            }
        }
    }

    private void addValueFull(CommandParametersImpl.Builder parameters,
                              Command command,
                              CommandPart part,
                              InjectedValueAccess injectedValues,
                              Consumer<CommandValueImpl.Builder> valueAdder) {
        parameters.addPresentPart(part);
        CommandValueImpl.Builder builder = CommandValueImpl.builder();
        valueAdder.accept(builder);
        parameters.addValue(part, builder
            .commandContext(command)
            .partContext(part)
            .injectedValues(injectedValues)
            .manager(this)
            .build());
    }
}
