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
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import org.enginehub.piston.Command;

/**
 * Parent class for all command-related exceptions.
 */
public class CommandException extends RuntimeException {
    private final Component message;
    protected final ImmutableList<Command> commands;

    public CommandException(ImmutableList<Command> commands) {
        this.message = TextComponent.empty();
        this.commands = commands;
    }

    public CommandException(Component message, ImmutableList<Command> commands) {
        super(message.toString());
        this.message = message;
        this.commands = commands;
    }

    public CommandException(Component message, Throwable cause, ImmutableList<Command> commands) {
        super(message.toString(), cause);
        this.message = message;
        this.commands = commands;
    }

    public CommandException(Throwable cause, ImmutableList<Command> commands) {
        super(cause);
        this.message = TextComponent.empty();
        this.commands = commands;
    }

    /**
     * Get the rich message, with extra formatting.
     */
    public Component getRichMessage() {
        return message;
    }

    /**
     * Retrieves all commands associated with this exception.
     *
     * <p>
     * This is intended to capture the parent command context upon exceptional state.
     * </p>
     */
    public ImmutableList<Command> getCommands() {
        return commands;
    }
}
