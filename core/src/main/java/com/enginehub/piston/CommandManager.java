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

package com.enginehub.piston;

import com.google.inject.Key;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Responsible for holding all commands, as well as parsing and dispatching from user input.
 */
public interface CommandManager {

    /**
     * Create a new command builder, using the default implementation of this manager.
     *
     * <p><strong>This does not register the command.</strong></p>
     *
     * @return the new command
     */
    Command.Builder newCommand(String name);

    /**
     * Registers any command, regardless of how it came about.
     *
     * <p>
     * Use this if you do not want to use the default command implementation of
     * this manager.
     * </p>
     *
     * @param command the command to register
     */
    void register(Command command);

    /**
     * Register a command that initially has the given name, and then is configured by a function.
     *
     * @param name the name of the command
     * @param registrationProcess a function that will build the command
     */
    default void register(String name, Consumer<Command.Builder> registrationProcess) {
        Command.Builder builder = newCommand(name);
        registrationProcess.accept(builder);
        register(builder.build());
    }

    /**
     * Register an entire manager with this one, inheriting all of its commands.
     */
    default void registerManager(CommandManager manager) {
        manager.getAllCommands().forEach(this::register);
    }

    /**
     * Inject a value into this manager. It will be provided by
     * {@link CommandParameters#injectedValue(Key)}.
     *
     * @param key the key for the value
     * @param supplier the supplier of the value
     * @param <T> the type of the value
     */
    <T> void injectValue(Key<T> key, Supplier<T> supplier);

    /**
     * Retrieve all commands that are registered.
     */
    Stream<Command> getAllCommands();

}
