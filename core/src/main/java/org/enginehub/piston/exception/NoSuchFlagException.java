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

package org.enginehub.piston.exception;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandParseResult;
import org.enginehub.piston.config.ColorConfig;
import org.enginehub.piston.part.CommandFlag;
import org.enginehub.piston.util.HelpGenerator;

import static java.util.stream.Collectors.joining;
import static org.enginehub.piston.util.StreamHelper.cast;

public class NoSuchFlagException extends UsageException {

    private static String getAllFlags(ImmutableList<Command> commands) {
        return cast(Iterables.getLast(commands).getParts().stream(), CommandFlag.class)
            .map(f -> String.valueOf(f.getName()))
            .collect(joining());
    }

    private static Component getMessage(CommandParseResult parseResult, char requestedFlag) {
        TextComponent.Builder message = Component.text();
        message.append(Component.text("Flag '"))
            .append(ColorConfig.mainText().wrap(String.valueOf(requestedFlag)))
            .append(Component.text("' is not a valid flag for "));
        message.append(HelpGenerator.create(parseResult).getFullName());
        String allFlags = getAllFlags(parseResult.getExecutionPath());
        if (allFlags.isEmpty()) {
            message.append(Component.text(", as it does not have any flags"));
        } else {
            message.append(Component.text(". Options: "))
                .append(ColorConfig.mainText().wrap(allFlags));
        }
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
