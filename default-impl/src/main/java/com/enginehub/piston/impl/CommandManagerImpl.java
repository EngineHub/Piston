package com.enginehub.piston.impl;

import com.enginehub.piston.Command;
import com.enginehub.piston.CommandManager;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class CommandManagerImpl implements CommandManager {
    private final Map<String, Command> commands = new HashMap<>();

    @Override
    public Command.Builder newCommand(String name) {
        return CommandImpl.builder(name);
    }

    @Override
    public void register(Command command) {
        commands.put(command.getName(), command);
    }

    @Override
    public Stream<Command> getAllCommands() {
        return commands.values().stream();
    }

}
