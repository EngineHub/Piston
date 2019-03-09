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

package org.enginehub.piston;

import com.google.common.collect.ImmutableMultiset;

class ReservedVariables {
    static final String COMMAND_MANAGER = "commandManager";
    static final String CONTAINER_INSTANCE = "containerInstance";
    static final String PARAMETERS = "parameters";

    /**
     * Names that are pre-reserved, and must not be used.
     */
    static ImmutableMultiset<String> names() {
        return ImmutableMultiset.of(
            COMMAND_MANAGER,
            CONTAINER_INSTANCE,
            PARAMETERS
        );
    }
}
