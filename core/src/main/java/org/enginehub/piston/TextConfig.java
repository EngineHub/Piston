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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Global text configuration.
 */
public class TextConfig {

    private static volatile String COMMAND_PREFIX = "";

    public static void setCommandPrefix(String commandPrefix) {
        COMMAND_PREFIX = checkNotNull(commandPrefix, "Command prefix cannot be `null`");
    }

    /**
     * Output command prefix -- all commands will be output with this prefix before their name.
     */
    public static String getCommandPrefix() {
        return COMMAND_PREFIX;
    }
}
