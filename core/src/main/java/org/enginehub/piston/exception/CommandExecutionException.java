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
import org.enginehub.piston.Command;

import javax.annotation.Nullable;

/**
 * Thrown when a command action throws an exception.
 *
 * <p>The thrown exception is the {@link #getCause() cause} of this exception</p>
 */
public class CommandExecutionException extends CommandException {

    public CommandExecutionException(Throwable cause, ImmutableList<Command> commands) {
        super(cause, commands);
    }

}
