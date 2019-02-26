package com.enginehub.piston;

import java.util.function.Consumer;
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
     * Retrieve all commands that are registered.
     */
    Stream<Command> getAllCommands();

}
