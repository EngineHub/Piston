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

import org.enginehub.piston.Command;
import org.enginehub.piston.part.CommandFlag;

import static java.util.stream.Collectors.joining;

public class NoSuchFlagException extends UsageException {

    private static String getAllFlags(Command command) {
        return command.getParts().stream()
            .filter(CommandFlag.class::isInstance)
            .map(f -> String.valueOf(((CommandFlag) f).getName()))
            .collect(joining());
    }

    private final char requestedFlag;

    public NoSuchFlagException(Command command, char requestedFlag) {
        super("Flag '" + requestedFlag + "' is not a valid flag for "
            + command.getName() + ". Options: " + getAllFlags(command), command);
        this.requestedFlag = requestedFlag;
    }

    public char getRequestedFlag() {
        return requestedFlag;
    }
}
