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

import net.kyori.text.TextComponent
import org.enginehub.piston.commands.RegressionCommands
import org.enginehub.piston.commands.RegressionCommandsRegistration
import org.enginehub.piston.exception.UsageException
import org.enginehub.piston.inject.InjectedValueAccess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

@DisplayName("Regression tests")
@Execution(ExecutionMode.CONCURRENT)
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
    @DisplayName("issue #9, regarding optionals in the middle of a command")
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

    private interface CString {
        operator fun invoke(str: String)
    }

    @Test
    @DisplayName("issue #9 part 2, regarding optionals mixing priority with sub-commands")
    fun issue9OptionalSubCommandPriority() {
        val action: CString = mock(CString::class.java)
        withRegressionCommands { _, manager ->
            manager.register("i9#2") { cmd ->
                val arg = arg("prior", "Arg prior") {
                    defaultsTo(listOf(""))
                }
                val sub = manager.newCommand("subcommand")
                        .action {
                            action(it.valueOf(arg).asString())
                            1
                        }
                        .description(TextComponent.of("Sub-command"))
                        .build()
                cmd.run {
                    description(TextComponent.of("Issue 9 #2"))
                    // Optional arg prior to sub-command
                    addPart(arg)
                    addPart(subs(sub))
                }
            }

            manager.execute(InjectedValueAccess.EMPTY, listOf("i9#2", "prior-opt", "subcommand"))

            verify(action)("prior-opt")
        }
    }

    @Test
    @DisplayName("issue #10, regarding arg flag positioning")
    fun issue10ArgFlagPositioning() {
        withRegressionCommands { ci, manager ->
            // verify [root] -p 1 [arg] works
            manager.execute(InjectedValueAccess.EMPTY, listOf("i10", "-p", "1", "req-arg"))
            // verify [root] [arg] -p 1 works
            manager.execute(InjectedValueAccess.EMPTY, listOf("i10", "req-arg", "-p", "1"))

            verify(ci, times(2)).i10("req-arg", 1)
        }
    }

    private val SUB_ACTION = 42
    private val ROOT_ACTION = 0x42

    @Test
    @DisplayName("issue #14, regarding optional sub-commands")
    fun issue14OptionalSubCommands() {
        withRegressionCommands { _, manager ->
            manager.register("i14") { cmd ->
                val req = arg("required", "Required argument, not needed if sub-command matches")
                val optAfter = arg("after", "Optional after sub-command, only matches without it") {
                    defaultsTo(listOf("default-value"))
                }
                val sub = manager.newCommand("vert")
                        .action { SUB_ACTION }
                        .description(TextComponent.of("Sub-command"))
                        .build()
                cmd.run {
                    action { ROOT_ACTION }
                    description(TextComponent.of("Issue 14"))
                    addPart(subs(sub, required = false))
                    addPart(req)
                    addPart(optAfter)
                }
            }

            assertEquals(
                    SUB_ACTION,
                    manager.execute(InjectedValueAccess.EMPTY, listOf("i14", "vert"))
            )
            assertEquals(
                    ROOT_ACTION,
                    manager.execute(InjectedValueAccess.EMPTY, listOf("i14", "10"))
            )
            assertEquals(
                    ROOT_ACTION,
                    manager.execute(InjectedValueAccess.EMPTY, listOf("i14", "10", "north"))
            )
        }
    }

    @Test
    @DisplayName("issue #18, regarding sub-command aliases")
    fun issue18SubCommandAliases() {
        withRegressionCommands { _, manager ->
            manager.register("hello") { cmd ->
                val sub = manager.newCommand("world")
                        .action { SUB_ACTION }
                        .aliases(setOf("there"))
                        .description(TextComponent.of("Sub-command"))
                        .build()
                cmd.run {
                    description(TextComponent.of("hello"))
                    addPart(subs(sub, required = true))
                }
            }

            assertEquals(
                    SUB_ACTION,
                    manager.execute(InjectedValueAccess.EMPTY, listOf("hello", "world"))
            )
            assertEquals(
                    SUB_ACTION,
                    manager.execute(InjectedValueAccess.EMPTY, listOf("hello", "there")) // general kenobi
            )
        }
    }
}
