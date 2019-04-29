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

package org.enginehub.piston.exception;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandParseResult;
import org.enginehub.piston.part.CommandFlag;
import org.enginehub.piston.util.HelpGenerator;

import static java.util.stream.Collectors.joining;

public class NoSuchFlagException extends UsageException {

    private static String getAllFlags(ImmutableList<Command> commands) {
        return Iterables.getLast(commands).getParts().stream()
            .filter(CommandFlag.class::isInstance)
            .map(f -> String.valueOf(((CommandFlag) f).getName()))
            .collect(joining());
    }

    private static Component getMessage(CommandParseResult parseResult, char requestedFlag) {
        TextComponent.Builder message = TextComponent.builder("");
        message.append(TextComponent.of("Flag '" + requestedFlag + "' is not a valid flag for "));
        message.append(HelpGenerator.create(parseResult).getFullName());
        String allFlags = getAllFlags(parseResult.getExecutionPath());
        message.append(TextComponent.of(
            allFlags.isEmpty()
                ? ", as it does not have any flags"
                : ". Options: " + allFlags
        ));
        return message.build();
    }

    private final char requestedFlag;

    public NoSuchFlagException(CommandParseResult parseResult, char requestedFlag) {
        super(getMessage(parseResult, requestedFlag), parseResult);
        this.requestedFlag = requestedFlag;
    }

    public char getRequestedFlag() {
        return requestedFlag;
    }
}
