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

package org.enginehub.piston

import org.enginehub.piston.commands.RegressionCommands
import org.enginehub.piston.commands.RegressionCommandsRegistration
import org.enginehub.piston.exception.UsageException
import org.enginehub.piston.inject.InjectedValueAccess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RegressionTest {

    private inline fun withRegressionCommands(block: (RegressionCommands, CommandManager) -> Unit) {
        withMockedContainer(RegressionCommands::class.java) { ci ->
            val manager = newManager().apply {
                installCommands(ci, RegressionCommandsRegistration.builder())
            }

            block(ci, manager)
        }
    }

    @Test
    @DisplayName("Regression test for issue #9, regarding optionals in the middle of a command")
    fun issue9MiddleOptionals() {
        withRegressionCommands { _, manager ->
            // Main issue: verify [root] [matching-second-not-first] [matching-first] fails
            // but not with a weird exception like ClassCast
            run {
                val usageEx = assertThrows<UsageException> {
                    manager.execute(InjectedValueAccess.EMPTY, listOf("i9", "5.5", "5"))
                }
                assertEquals("Too many arguments.", usageEx.message)
            }

            // Second issue: verify [root] [not-matching-any] [extra] fails with a correct usage ex
            // we want it to pick 5.x as the _second_ argument, no TMA ex like the previous case
            run {
                val usageEx = assertThrows<UsageException> {
                    manager.execute(InjectedValueAccess.EMPTY, listOf("i9", "5.x", "f"))
                }
                assertEquals("Invalid value for <piston.argument.second> (For input string: \"5.x\")," +
                        " acceptable values are any double", usageEx.message)
            }
        }

    }
}
