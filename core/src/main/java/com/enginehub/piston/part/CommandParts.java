package com.enginehub.piston.part;

import com.enginehub.piston.converter.ArgumentConverters;

public class CommandParts {

    public static NoArgCommandFlag.Builder flag(char flag, String description) {
        return NoArgCommandFlag.builder(flag, description);
    }

    public static CommandArgument.Builder<String> arg(String name, String description) {
        return CommandArgument.builder(name, description, ArgumentConverters.forString());
    }

    private CommandParts() {
    }
}
