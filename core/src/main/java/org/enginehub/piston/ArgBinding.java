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

import com.google.common.collect.ImmutableSet;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.part.CommandPart;

public interface ArgBinding {

    /**
     * Get the raw input that was bound.
     */
    String getInput();

    /**
     * Did we match the given part exactly?
     *
     * @param part the part, must be contained in the parts returned by {@link #getParts()}
     * @since 0.5.8
     */
    default boolean isExactMatch(CommandPart part) {
        return true;
    }

    /**
     * Get the part(s) the input was bound to.
     */
    ImmutableSet<CommandPart> getParts();

}
