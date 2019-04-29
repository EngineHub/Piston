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

import net.kyori.text.Component;
import org.enginehub.piston.CommandParseResult;

/**
 * Parent class for all usage-related exceptions.
 *
 * Provides a partially complete parsing result to assist in providing better help messages.
 */
public class UsageException extends CommandException {
    private final CommandParseResult commandParseResult;

    public UsageException(CommandParseResult commandParseResult) {
        super(commandParseResult.getExecutionPath());
        this.commandParseResult = commandParseResult;
    }

    public UsageException(Component message, CommandParseResult commandParseResult) {
        super(message, commandParseResult.getExecutionPath());
        this.commandParseResult = commandParseResult;
    }

    public UsageException(Component message, Throwable cause, CommandParseResult commandParseResult) {
        super(message, cause, commandParseResult.getExecutionPath());
        this.commandParseResult = commandParseResult;
    }

    public UsageException(Throwable cause, CommandParseResult commandParseResult) {
        super(cause, commandParseResult.getExecutionPath());
        this.commandParseResult = commandParseResult;
    }

    public CommandParseResult getCommandParseResult() {
        return commandParseResult;
    }
}
