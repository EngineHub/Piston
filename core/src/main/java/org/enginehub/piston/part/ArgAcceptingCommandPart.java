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

package org.enginehub.piston.part;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.CommandValue;
import org.enginehub.piston.inject.Key;

public interface ArgAcceptingCommandPart extends CommandPart {

    default CommandValue value(CommandParameters parameters) {
        return parameters.valueOf(this);
    }

    /**
     * All possible types for this argument. This allows for completions to
     * be filled from converters registered with the manager.
     *
     * <p>
     * This set may be empty, in which case there will be no completions.
     * </p>
     */
    ImmutableSet<Key<?>> getTypes();

    ImmutableList<String> getDefaults();

}
