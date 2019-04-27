package org.enginehub.piston.impl;

import com.google.common.collect.ImmutableList;
import org.enginehub.piston.Command;
import org.enginehub.piston.inject.InjectedValueAccess;

import java.util.ArrayList;
import java.util.List;

class CommandParser {

    private final List<String> calledNames = new ArrayList<>();
    private final List<Command> executionPath = new ArrayList<>();
    private final ImmutableList<String> arguments;
    private final int argumentIndex = 0;
    private final InjectedValueAccess context;

    CommandParser(Command initial, Iterable<String> arguments, InjectedValueAccess context) {
        this.arguments = ImmutableList.copyOf(arguments);
        this.context = context;
    }



}
