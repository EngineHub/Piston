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

package org.enginehub.piston.part;

import net.kyori.adventure.text.Component;
import org.enginehub.piston.CommandParameters;

/**
 * Represents a part of a command, i.e. anything that can be found on the command line: flags,
 * arguments, and commands themselves.
 */
public interface CommandPart {

    default boolean in(CommandParameters parameters) {
        return parameters.has(this);
    }

    /**
     * Returns the text representation of this part. This is used to show
     * the user where the part belongs, and what it should look like.
     *
     * <p>
     * For flags, this could be something like `-flag`.
     * For arguments and commands, this could be their name.
     * </p>
     */
    Component getTextRepresentation();

    /**
     * Returns the description of this part. This should describe what
     * the part controls in the command's execution.
     */
    Component getDescription();

    /**
     * Returns {@code true} if this part is required, and may not be
     * missing from the command line.
     */
    boolean isRequired();
}
