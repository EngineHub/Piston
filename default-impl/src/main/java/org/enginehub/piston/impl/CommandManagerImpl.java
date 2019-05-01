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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandMetadata;
import org.enginehub.piston.CommandParseResult;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ArgumentConverters;
import org.enginehub.piston.exception.NoSuchCommandException;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.inject.MemoizingValueAccess;
import org.enginehub.piston.part.SubCommandPart;
import org.enginehub.piston.suggestion.Suggestion;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;
import static org.enginehub.piston.converter.SuggestionHelper.byPrefix;

public class CommandManagerImpl implements CommandManager {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<String, Command> commands = new HashMap<>();
    private final Map<Key<?>, ArgumentConverter<?>> converters = new HashMap<>();
    private final CommandInfoCache commandInfoCache = new CommandInfoCache();

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
        validateAndCache(command, new HashSet<>());
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

    private void validateAndCache(Command command, Set<Command> seen) {
        if (!seen.add(command)) {
            throw new IllegalStateException("Self-referential command");
        }
        commandInfoCache.getInfo(command);
        // validate sub-commands too
        command.getParts().stream()
            .filter(p -> p instanceof SubCommandPart)
            .flatMap(p -> ((SubCommandPart) p).getCommands().stream())
            .forEach(c -> validateAndCache(c, seen));
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
    public Stream<Command> getAllCommands() {
        ImmutableSet<Command> allCommands;
        lock.readLock().lock();
        try {
            allCommands = ImmutableSet.copyOf(commands.values());
        } finally {
            lock.readLock().unlock();
        }
        return allCommands.stream();
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
    public ImmutableSet<Suggestion> getSuggestions(InjectedValueAccess context, List<String> args) {
        Command command;
        CommandParseResult parseResult;
        lock.readLock().lock();
        try {
            String name = args.get(0);
            command = commands.get(name);
            if (command == null) {
                // suggest on commands instead
                return suggestCommands(name);
            }
            // parse also locks -- re-entrant lock required for this
            parseResult = parse(context, args);
        } finally {
            lock.readLock().unlock();
        }
        List<String> reconstructedArguments = parseResult.getOriginalArguments();
        // This makes no sense. Throw this case away.
        checkState(reconstructedArguments.size() <= args.size(),
            "Reconstructed arguments list bigger than original args list");
        // And ask the command to suggest. In most cases this uses the default suggester.
        return command.getSuggester().provideSuggestions(args, parseResult);
    }

    private ImmutableSet<Suggestion> suggestCommands(String name) {
        return ImmutableSet.copyOf(
            getAllCommands()
                .map(Command::getName)
                .filter(byPrefix(name))
                .map(s -> Suggestion.builder()
                    .suggestion(s)
                    .replacedArgument(0)
                    .build())
                .iterator()
        );
    }

    @Override
    public CommandParseResult parse(InjectedValueAccess context, List<String> args) {
        lock.readLock().lock();
        try {
            String name = args.get(0);
            Command command = commands.get(name);
            if (command == null) {
                throw new NoSuchCommandException(name);
            }
            // cache if needed
            InjectedValueAccess cachedContext = MemoizingValueAccess.wrap(context);
            CommandMetadata metadata = CommandMetadataImpl.builder()
                .calledName(name)
                .arguments(ImmutableList.copyOf(args.subList(1, args.size())))
                .build();
            return new CommandParser(
                this, commandInfoCache, command, metadata, cachedContext
            ).parse();
        } finally {
            lock.readLock().unlock();
        }
    }

}
