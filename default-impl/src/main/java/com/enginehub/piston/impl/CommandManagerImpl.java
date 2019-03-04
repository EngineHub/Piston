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

package com.enginehub.piston.impl;

import com.enginehub.piston.Command;
import com.enginehub.piston.CommandManager;
import com.enginehub.piston.converter.ArgumentConverter;
import com.google.inject.Key;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class CommandManagerImpl implements CommandManager {
    private final Map<String, Command> commands = new ConcurrentHashMap<>();
    private final Map<Key<?>, Supplier<?>> injectedValues = new ConcurrentHashMap<>();
    private final Map<Key<?>, ArgumentConverter<?>> converters = new ConcurrentHashMap<>();

    @Override
    public Command.Builder newCommand(String name) {
        return CommandImpl.builder(name);
    }

    @Override
    public void register(Command command) {
        commands.put(command.getName(), command);
    }

    @Override
    public <T> void registerConverter(Key<T> key, ArgumentConverter<T> converter) {
        converters.put(key, converter);
    }

    @Override
    public <T> Collection<T> convert(String value, Key<T> key) {
        @SuppressWarnings("unchecked")
        ArgumentConverter<T> converter = (ArgumentConverter<T>) converters.get(key);
        if (converter == null) {
            throw new NoSuchElementException("No converter for " + key);
        }
        return requireNonNull(converter.convert(value), "Converter may not return null.");
    }

    @Override
    public Stream<Command> getAllCommands() {
        return commands.values().stream();
    }

    @Override
    public int execute(String[] args) {
        return 0;
    }

    @Override
    public <T> void injectValue(Key<T> key, Supplier<T> supplier) {
        injectedValues.put(key, supplier);
    }
}
