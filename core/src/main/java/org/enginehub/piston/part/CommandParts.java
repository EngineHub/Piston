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
import net.kyori.adventure.text.TranslatableComponent;

public class CommandParts {

    public static NoArgCommandFlag.Builder flag(char flag, Component description) {
        return NoArgCommandFlag.builder(flag, description);
    }

    public static CommandArgument.Builder arg(TranslatableComponent name, Component description) {
        return CommandArgument.builder(name, description);
    }

    private CommandParts() {
    }
}
