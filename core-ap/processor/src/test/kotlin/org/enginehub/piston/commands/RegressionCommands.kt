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

package org.enginehub.piston.commands

import org.enginehub.piston.annotation.Command
import org.enginehub.piston.annotation.CommandContainer
import org.enginehub.piston.annotation.param.Arg
import org.enginehub.piston.annotation.param.ArgFlag
import org.enginehub.piston.annotation.param.Switch

@CommandContainer
interface RegressionCommands {
    @Command(name = "i9", desc = "description")
    fun i9(
        @Arg(desc = "First argument", def = [""]) first: Int?,
        @Arg(desc = "Second argument") second: Double
    )

    @Command(name = "i10", desc = "description")
    fun i10(
        @Arg(desc = "First argument") first: String,
        @ArgFlag(name = 'p', desc = "Page number") page: Int?
    )

    @Command(name = "i29", desc = "description")
    fun i29(
        @Arg(desc = "First argument", def = [""]) first: String?,
        @ArgFlag(name = 'm', desc = "Mask") mask: Int?
    )

    @Command(name = "i30", desc = "description")
    fun i30(
        @Arg(desc = "First argument", def = [""]) first: String?,
        @ArgFlag(name = 'm', desc = "Mask") mask: Int?,
        @Switch(name = 'g', desc = "Is good") good: Boolean
    )
}
