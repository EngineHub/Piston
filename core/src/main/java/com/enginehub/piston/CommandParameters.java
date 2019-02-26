/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) EngineHub <http://www.enginehub.com>
 * Copyright (C) oblique-commands contributors
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

package com.enginehub.piston;

import com.enginehub.piston.part.ArgAcceptingCommandPart;
import com.enginehub.piston.part.CommandPart;

import javax.annotation.Nullable;
import java.util.List;

public interface CommandParameters<C> {

    /**
     * Determine if the execution will also include a sub-command.
     */
    boolean isRunningSubcommand();

    /**
     * Checks if the parameters contain the specified part.
     *
     * @param part - the part to look for
     * @return if the parameters contain the specified part
     */
    boolean has(CommandPart part);

    /**
     * Gets the value of the specified part, throwing if it
     * is not {@linkplain #has(CommandPart) present} or if
     * there are multiple values.
     *
     * @param part - the part to look for
     * @return the value
     */
    <T> T valueOf(ArgAcceptingCommandPart<T> part);

    /**
     * Gets all the values of the specified part, throwing if it
     * is not {@linkplain #has(CommandPart) present}.
     *
     * @param part - the part to look for
     * @return the value
     */
    <T> List<T> valuesOf(ArgAcceptingCommandPart<T> part);

    /**
     * Get the command context.
     */
    @Nullable
    C getContext();

}
