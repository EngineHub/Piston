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

import net.kyori.text.TextComponent
import org.enginehub.piston.Command
import org.enginehub.piston.CommandManager
import org.enginehub.piston.assertEqualUnordered
import org.enginehub.piston.inject.InjectedValueAccess
import org.enginehub.piston.inject.Key
import org.enginehub.piston.installCommands
import org.enginehub.piston.newManager
import org.enginehub.piston.subs
import org.enginehub.piston.withMockedContainer
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.streams.toList

@DisplayName("The manager's suggestion provider")
class ManagerSuggestionTest {

    private val suggestionMatrix = listOf(
            setOf("art", "buck", "chair", "axe", "barely", "carrots"),
            setOf("123", "244", "312", "100", "213", "333"),
            setOf("something", "anything", "really"),
            // attempt to trigger a bug where completions go too far:
            setOf("something_else", "anything_else", "really_not_it"),
            setOf("something_else_entirely", "anything_else_except_this", "really_off")
    )

    private fun withSuggestionManager(block: (CommandManager) -> Unit) {
        withMockedContainer<SuggestingCommand> {
            val manager = newManager().apply {
                installCommands(it, SuggestingCommandRegistration.builder())
                suggestionMatrix.forEachIndexed { index, set ->
                    registerConverter(Key.of(String::class.java, suggest(index + 1)),
                            SimpleSuggestingConverter(set.toList()))
                }

                register("notpermitted") { cmd ->
                    cmd.description(TextComponent.of("Command with false condition"))
                    cmd.condition(Command.Condition.FALSE)
                }

                register("sub") { cmd ->
                    cmd.description(TextComponent.of("Sub-commands test command"))
                    cmd.addPart(subs(*allCommands.toList().toTypedArray()))
                }
            }
            block(manager)
        }
    }

    private fun byPrefix(prefix: String): (String) -> Boolean {
        return { it.length > prefix.length && it.startsWith(prefix, ignoreCase = true) }
    }

    @Test
    @DisplayName("suggests commands on empty input")
    fun suggestsCommandOnEmpty() {
        withSuggestionManager { manager ->
            val actualSuggestions = manager.getSuggestions(InjectedValueAccess.EMPTY, listOf(""))
            assertEqualUnordered(manager.allCommands.map { it.name }.filter { it != "notpermitted" }.toList(),
                actualSuggestions.map { it.suggestion })
            assertTrue(actualSuggestions.all { it.replacedArgument == 0 }, "replacement not targeted at first argument")
        }
    }

    @Test
    @DisplayName("suggests other-manager-registered commands on empty input")
    fun suggestsOtherManagerRegisteredCommandsOnEmpty() {
        withSuggestionManager { manager ->
            manager.registerManager(newManager().apply {
                register("permitted") { cmd ->
                    cmd.description(TextComponent.of("Command with true condition"))
                    cmd.condition(Command.Condition.TRUE)
                }
            })
            val actualSuggestions = manager.getSuggestions(InjectedValueAccess.EMPTY, listOf(""))
            assertTrue(actualSuggestions.map { it.suggestion }.any { it == "permitted" }) {
                "`permitted` not contained in suggestions"
            }
            assertEqualUnordered(manager.allCommands.map { it.name }.filter { it != "notpermitted" }.toList(),
                actualSuggestions.map { it.suggestion })
            assertTrue(actualSuggestions.all { it.replacedArgument == 0 }, "replacement not targeted at first argument")
        }
    }

    @Test
    @DisplayName("suggests first argument if command selected")
    fun suggestsFirstOnCommandSelected() {
        withSuggestionManager { manager ->
            val actualSuggestions = manager.getSuggestions(InjectedValueAccess.EMPTY, listOf("cmd"))
            assertEqualUnordered(suggestionMatrix[0], actualSuggestions.map { it.suggestion })
            assertTrue(actualSuggestions.all { it.replacedArgument == 1 }, "replacement not targeted at second argument")
        }
    }

    @Test
    @DisplayName("suggests matching parts of first argument if first partially filled")
    fun suggestsSubFirst() {
        withSuggestionManager { manager ->
            val actualSuggestions = manager.getSuggestions(InjectedValueAccess.EMPTY, listOf("cmd", "a"))
            assertEqualUnordered(
                    suggestionMatrix[0].filter(byPrefix("a")),
                    actualSuggestions.map { it.suggestion })
            assertTrue(actualSuggestions.all { it.replacedArgument == 1 }, "replacement not targeted at second argument")
        }
    }

    @Test
    @DisplayName("suggests second and third argument if command & first selected")
    fun suggestsSecondAndThird() {
        withSuggestionManager { manager ->
            val actualSuggestions = manager.getSuggestions(InjectedValueAccess.EMPTY,
                    listOf("cmd", suggestionMatrix[0].first()))
            assertEqualUnordered(suggestionMatrix[1] + suggestionMatrix[2],
                    actualSuggestions.map { it.suggestion })
            assertTrue(actualSuggestions.all { it.replacedArgument == 2 }, "replacement not targeted at third argument")
        }
    }

    @Test
    @DisplayName("suggests parts of second and third argument if already selected")
    fun suggestsSubSecondAndThird() {
        withSuggestionManager { manager ->
            assertAll({
                val actualSuggestions = manager.getSuggestions(InjectedValueAccess.EMPTY,
                        listOf("cmd", suggestionMatrix[0].first(), "1"))
                assertEqualUnordered((suggestionMatrix[1] + suggestionMatrix[2]).filter(byPrefix("1")),
                        actualSuggestions.map { it.suggestion })
                assertTrue(actualSuggestions.all { it.replacedArgument == 2 }, "replacement not targeted at third argument")
            }, {
                val actualSuggestions = manager.getSuggestions(InjectedValueAccess.EMPTY,
                        listOf("cmd", suggestionMatrix[0].first(), "someth"))
                assertEqualUnordered((suggestionMatrix[1] + suggestionMatrix[2]).filter(byPrefix("someth")),
                        actualSuggestions.map { it.suggestion })
                assertTrue(actualSuggestions.all { it.replacedArgument == 2 }, "replacement not targeted at third argument")
            })
        }
    }

    @Test
    @DisplayName("suggests flags on `-` input")
    fun suggestsFlag() {
        withSuggestionManager { manager ->
            val actualSuggestions = manager.getSuggestions(InjectedValueAccess.EMPTY,
                    listOf("flags", "-"))
            assertEqualUnordered(setOf("-1", "-2", "-3"), actualSuggestions.map { it.suggestion })
            assertTrue(actualSuggestions.all { it.replacedArgument == 1 }, "replacement not targeted at second argument")
        }
    }

    @Test
    @DisplayName("suggests only unused flags on `-1` input")
    fun suggestsOnlyUnusedFlag() {
        withSuggestionManager { manager ->
            val actualSuggestions = manager.getSuggestions(InjectedValueAccess.EMPTY,
                    listOf("flags", "-1"))
            assertEqualUnordered(setOf("-12", "-13"), actualSuggestions.map { it.suggestion })
            assertTrue(actualSuggestions.all { it.replacedArgument == 1 }, "replacement not targeted at second argument")
        }
    }

    @Test
    @DisplayName("suggests argument for argument flags")
    fun suggestsArgumentForArgumentFlags() {
        withSuggestionManager { manager ->
            val actualSuggestions = manager.getSuggestions(InjectedValueAccess.EMPTY,
                    listOf("flags", "-3"))
            assertEqualUnordered(suggestionMatrix[2], actualSuggestions.map { it.suggestion })
            assertTrue(actualSuggestions.all { it.replacedArgument == 2 }, "replacement not targeted at third argument")
        }
    }

    @Test
    @DisplayName("suggests parts of arguments for argument flags")
    fun suggestsSubArgumentForArgumentFlags() {
        withSuggestionManager { manager ->
            val actualSuggestions = manager.getSuggestions(InjectedValueAccess.EMPTY,
                    listOf("flags", "-3", "any"))
            assertEqualUnordered(suggestionMatrix[2].filter(byPrefix("any")),
                    actualSuggestions.map { it.suggestion })
            assertTrue(actualSuggestions.all { it.replacedArgument == 2 }, "replacement not targeted at third argument")
        }
    }

    @Test
    @DisplayName("suggests sub-commands")
    fun suggestsSubCommands() {
        withSuggestionManager { manager ->
            val actualSuggestions = manager.getSuggestions(InjectedValueAccess.EMPTY,
                    listOf("sub"))
            assertEqualUnordered(setOf("cmd", "flags"), actualSuggestions.map { it.suggestion })
            assertTrue(actualSuggestions.all { it.replacedArgument == 1 }, "replacement not targeted at second argument")
        }
    }

    @Test
    @DisplayName("suggests partial sub-commands")
    fun suggestsSubSubCommands() {
        withSuggestionManager { manager ->
            val actualSuggestions = manager.getSuggestions(InjectedValueAccess.EMPTY,
                    listOf("sub", "c"))
            assertEqualUnordered(setOf("cmd"), actualSuggestions.map { it.suggestion })
            assertTrue(actualSuggestions.all { it.replacedArgument == 1 }, "replacement not targeted at second argument")
        }
    }

    @Test
    @DisplayName("suggests sub-command's arguments")
    fun suggestsSubCommandArguments() {
        withSuggestionManager { manager ->
            val actualSuggestions = manager.getSuggestions(InjectedValueAccess.EMPTY,
                    listOf("sub", "cmd"))
            assertEqualUnordered(suggestionMatrix[0], actualSuggestions.map { it.suggestion })
            assertTrue(actualSuggestions.all { it.replacedArgument == 2 }, "replacement not targeted at thrid argument")
        }
    }
}
