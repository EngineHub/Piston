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
import com.google.common.collect.ImmutableMap;
import org.enginehub.piston.Command;
import org.enginehub.piston.part.ArgAcceptingCommandFlag;
import org.enginehub.piston.part.ArgAcceptingCommandPart;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.CommandFlag;
import org.enginehub.piston.part.CommandPart;
import org.enginehub.piston.part.NoArgCommandFlag;
import org.enginehub.piston.part.SubCommandPart;

import java.util.Optional;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

class CommandInfo {

    static CommandInfo from(Command command) {
        ImmutableList.Builder<CommandArgument> arguments = ImmutableList.builder();
        ImmutableList.Builder<ArgAcceptingCommandPart> defaultProvided = ImmutableList.builder();
        ImmutableMap.Builder<Character, CommandFlag> flags = ImmutableMap.builder();
        ImmutableMap.Builder<String, Command> subCommands = ImmutableMap.builder();
        Optional<SubCommandPart> subCommandPart = Optional.empty();
        int subCommandArgIndex = -1;
        boolean seenOptionalArg = false;
        boolean middleOptionalArg = false;
        ImmutableList<CommandPart> parts = command.getParts();
        int requiredParts = 0;
        for (int i = 0; i < parts.size(); i++) {
            CommandPart part = parts.get(i);
            if (part instanceof ArgAcceptingCommandFlag || part instanceof NoArgCommandFlag) {
                CommandFlag flag = (CommandFlag) part;
                flags.put(flag.getName(), flag);
            } else if (part instanceof CommandArgument) {
                if (part.isRequired() && seenOptionalArg) {
                    middleOptionalArg = true;
                }
                if (!part.isRequired()) {
                    seenOptionalArg = true;
                }
                arguments.add((CommandArgument) part);
            } else if (part instanceof SubCommandPart) {
                if (part.isRequired()) {
                    checkState(i + 1 >= parts.size(),
                        "Required sub-command must be last part.");
                }
                SubCommandPart scp = (SubCommandPart) part;
                for (Command cmd : scp.getCommands()) {
                    subCommands.put(cmd.getName(), cmd);
                    for (String alias : cmd.getAliases()) {
                        subCommands.put(alias, cmd);
                    }
                }
                subCommandPart = Optional.of(scp);
                subCommandArgIndex = requiredParts;
            } else {
                throw new IllegalStateException("Unknown part implementation " + part);
            }
            if (part.isRequired()) {
                requiredParts++;
            }
            if (part instanceof ArgAcceptingCommandPart) {
                ArgAcceptingCommandPart argPart = (ArgAcceptingCommandPart) part;
                if (argPart.getDefaults().size() > 0) {
                    defaultProvided.add(argPart);
                }
            }
        }
        // guards against a specific, hard-to-solve, edge-case
        checkState(!(subCommandPart.isPresent() && middleOptionalArg),
            "Cannot have middle-filled optionals and sub-commands");
        ImmutableList<CommandArgument> commandArguments = arguments.build();
        int[] indexes = IntStream.range(0, commandArguments.size())
            .filter(idx -> commandArguments.get(idx).isVariable())
            .toArray();
        checkArgument(indexes.length <= 1, "Too many variable arguments");
        if (indexes.length > 0) {
            int varargIndex = indexes[0];
            checkArgument(varargIndex == commandArguments.size() - 1,
                "Variable argument must be the last argument");
        }
        return new CommandInfo(
            commandArguments,
            defaultProvided.build(),
            flags.build(),
            subCommands.build(),
            subCommandPart,
            requiredParts,
            subCommandArgIndex);
    }

    final ImmutableList<CommandArgument> arguments;
    final ImmutableList<ArgAcceptingCommandPart> defaultProvided;
    final ImmutableMap<Character, CommandFlag> flags;
    final ImmutableMap<String, Command> subCommands;
    final Optional<SubCommandPart> subCommandPart;
    final int requiredParts;
    /**
     * Index where the sub-command is placed. The number of required arguments consumed
     * should be <em>equal</em> to this for the sub-command to be matched.
     */
    final int subCommandArgIndex;

    CommandInfo(ImmutableList<CommandArgument> arguments,
                ImmutableList<ArgAcceptingCommandPart> defaultProvided,
                ImmutableMap<Character, CommandFlag> flags,
                ImmutableMap<String, Command> subCommands,
                Optional<SubCommandPart> subCommandPart,
                int requiredParts,
                int subCommandArgIndex) {
        this.arguments = arguments;
        this.defaultProvided = defaultProvided;
        this.flags = flags;
        this.subCommands = subCommands;
        this.subCommandPart = subCommandPart;
        this.requiredParts = requiredParts;
        this.subCommandArgIndex = subCommandArgIndex;
    }
}
