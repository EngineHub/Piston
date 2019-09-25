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

package org.enginehub.piston;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.enginehub.piston.converter.ArgumentConverterStore;
import org.enginehub.piston.exception.CommandException;
import org.enginehub.piston.exception.CommandExecutionException;
import org.enginehub.piston.exception.ConditionFailedException;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.suggestion.Suggestion;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Responsible for holding all commands, as well as parsing and dispatching from user input.
 */
public interface CommandManager extends ArgumentConverterStore {

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
     * Retrieve all commands that are registered.
     */
    Stream<Command> getAllCommands();

    /**
     * Determine if this manager knows of a command with name {@code name}.
     *
     * <p>Includes aliases.</p>
     *
     * @param name the name to check
     * @return {@code true} if a command has this name, either primarily or by alias
     */
    default boolean containsCommand(String name) {
        return getCommand(name).isPresent();
    }

    /**
     * Get the command with name {@code name}, if registered.
     *
     * <p>Includes aliases.</p>
     *
     * @param name the name to check
     * @return {@link Optional#of(Object)} the command if registered,
     *     otherwise {@link Optional#empty()}
     */
    Optional<Command> getCommand(String name);

    /**
     * Suggest inputs based on a current user input. This input should be the
     * entire command line, so that partial parsing may occur.
     *
     * @param context the injected value context
     * @param args the command line to suggest into
     * @return the suggestions
     */
    ImmutableSet<Suggestion> getSuggestions(InjectedValueAccess context, List<String> args);

    /**
     * Parse a command, given a set of arguments and a context.
     *
     * <p>
     * Argument zero is the command name, without any leading slash.
     * The rest of the arguments will be parsed into the correct parts.
     * </p>
     *
     * <p>
     * Note: Command conditions <em>must</em> pass when encountered,
     * e.g. the root command condition will be tested <em>before</em>
     * parsing its arguments. This is to ensure no information is leaked
     * about sub-commands, etc. while parsing. This also means that there
     * is no need to validate conditions after calling this method,
     * unless you wish to use a different context.
     * </p>
     *
     * @param context the injected value context
     * @param args the arguments to include
     * @return the parsing output
     */
    CommandParseResult parse(InjectedValueAccess context, List<String> args);

    /**
     * Execute a command, given a set of arguments and a context.
     *
     * <p>
     * Argument zero is the command name, without any leading slash.
     * The rest of the arguments will be parsed into the correct parts.
     * </p>
     *
     * @param context the injected value context
     * @param args the arguments to include
     * @return the count from the executed command
     */
    default int execute(InjectedValueAccess context, List<String> args) {
        CommandParseResult parse = parse(context, args);

        try {
            return parse.getPrimaryCommand().getAction().run(parse.getParameters());
        } catch (CommandException e) {
            throw e;
        } catch (Exception e) {
            throw new CommandExecutionException(e, parse.getExecutionPath());
        }
    }

}
