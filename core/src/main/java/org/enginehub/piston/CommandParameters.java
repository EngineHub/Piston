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

package org.enginehub.piston;

import org.enginehub.piston.converter.ArgumentConverterAccess;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.part.ArgAcceptingCommandPart;
import org.enginehub.piston.part.CommandPart;

import javax.annotation.Nullable;

/**
 * Access to part values derived from user input.
 */
public interface CommandParameters extends InjectedValueAccess {

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
    CommandValue valueOf(ArgAcceptingCommandPart part);

    /**
     * Gets the metadata for the command being called.
     *
     * @return the command metadata, if it exists
     */
    @Nullable
    CommandMetadata getMetadata();

    /**
     * Get the converters used in this call.
     */
    ArgumentConverterAccess getConverters();

}
