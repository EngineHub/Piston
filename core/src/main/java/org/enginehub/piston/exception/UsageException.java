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

/**
 * Parent class for all usage-related exceptions.
 */
public class UsageException extends CommandException {
    public UsageException(Command command) {
        super(command);
    }

    public UsageException(String message, Command command) {
        super(message, command);
    }

    public UsageException(String message, Throwable cause, Command command) {
        super(message, cause, command);
    }

    public UsageException(Throwable cause, Command command) {
        super(cause, command);
    }
}
