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

package org.enginehub.piston.suggestion

import org.enginehub.piston.annotation.Command
import org.enginehub.piston.annotation.CommandContainer
import org.enginehub.piston.annotation.param.Arg
import org.enginehub.piston.annotation.param.ArgFlag
import org.enginehub.piston.annotation.param.Switch

@CommandContainer
interface SuggestingCommand {
    @Command(name = "cmd", desc = "description")
    fun cmd(@[Arg(desc = "First argument") Suggest(1)] first: String,
            @[Arg(desc = "Optional second argument", def = [""]) Suggest(2)] second: String,
            @[Arg(desc = "Required third argument") Suggest(3)] third: String,
            @[Arg(desc = "Required fourth argument") Suggest(4)] fourth: String)

    @Command(name = "flags", desc = "flag test command")
    fun flags(@[Switch(name = '1', desc = "First flag")] first: Boolean,
              @[Switch(name = '2', desc = "Second flag")] second: Boolean,
              @[ArgFlag(name = '3', desc = "Argument-taking third flag") Suggest(3)] third: String)
}
