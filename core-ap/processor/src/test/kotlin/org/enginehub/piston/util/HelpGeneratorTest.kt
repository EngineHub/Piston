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

package org.enginehub.piston.util

import com.google.common.collect.ImmutableList
import net.kyori.text.TextComponent
import org.enginehub.piston.TestCommandMetadata
import org.enginehub.piston.TestCommandParameters
import org.enginehub.piston.TestParseResult
import org.enginehub.piston.arg
import org.enginehub.piston.argFlag
import org.enginehub.piston.bind
import org.enginehub.piston.commands.NoArgCommand
import org.enginehub.piston.commands.NoArgCommandRegistration
import org.enginehub.piston.commands.SingleArgCommand
import org.enginehub.piston.commands.SingleArgCommandRegistration
import org.enginehub.piston.commands.SingleOptionalArgCommand
import org.enginehub.piston.commands.SingleOptionalArgCommandRegistration
import org.enginehub.piston.flag
import org.enginehub.piston.installCommands
import org.enginehub.piston.newManager
import org.enginehub.piston.subs
import org.enginehub.piston.withMockedContainer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.streams.toList

@DisplayName("HelpGenerator")
class HelpGeneratorTest {

    @Test
    fun noArgHelp() {
        withMockedContainer<NoArgCommand> { ci ->
            val manager = newManager().apply {
                installCommands(ci, NoArgCommandRegistration.builder())
            }

            val command = manager.allCommands.toList()
            assertEquals("""
                description
                Usage: no-arg
            """.trimIndent(), TextHelper.reduceToText(HelpGenerator.create(command).fullHelp))
        }
    }

    @Test
    fun singleArgHelp() {
        withMockedContainer<SingleArgCommand> { ci ->
            val manager = newManager().apply {
                installCommands(ci, SingleArgCommandRegistration.builder())
            }

            val command = manager.allCommands.toList()
            assertEquals("""
                description
                Usage: single-arg <piston.argument.first>
                Arguments:
                  <piston.argument.first>: First argument
            """.trimIndent(), TextHelper.reduceToText(HelpGenerator.create(command).fullHelp))
        }
    }

    @Test
    fun singleArgOptionalHelp() {
        withMockedContainer<SingleOptionalArgCommand> { ci ->
            val manager = newManager().apply {
                installCommands(ci, SingleOptionalArgCommandRegistration.builder())
            }

            val command = manager.allCommands.toList()
            assertEquals("""
                description
                Usage: single-arg-opt [piston.argument.first]
                Arguments:
                  [piston.argument.first] (defaults to none): First argument
            """.trimIndent(), TextHelper.reduceToText(HelpGenerator.create(command).fullHelp))
        }
    }

    @Test
    fun singleArgOptionalNotNoneHelp() {
        val command = listOf(newManager().newCommand("single-arg-opt")
            .description(TextComponent.of("description"))
            .addParts(
                arg("piston.argument.first","First argument") {
                    defaultsTo(ImmutableList.of("a", "b"))
                }
            )
            .build())
        assertEquals("""
            description
            Usage: single-arg-opt [piston.argument.first]
            Arguments:
              [piston.argument.first] (defaults to [a, b]): First argument
        """.trimIndent(), TextHelper.reduceToText(HelpGenerator.create(command).fullHelp))
    }

    @Test
    fun flagsHelp() {
        val command = listOf(newManager().newCommand("flags")
            .description(TextComponent.of("description"))
            .addParts(
                flag('f', "Flag"),
                argFlag('q', "Quibble", "qux"),
                argFlag('b', "Bizarre", "baz") {
                    defaultsTo(ImmutableList.of("", "nonblank"))
                }
            )
            .build())
        assertEquals("""
            description
            Usage: flags [-f] [-q <qux>] [-b <baz>]
            Flags:
              -f: Flag
              -q: Quibble
              -b (defaults to [nonblank]): Bizarre
        """.trimIndent(), TextHelper.reduceToText(HelpGenerator.create(command).fullHelp))
    }

    @Test
    fun subCommandsHelp() {
        val subCommand = newManager().newCommand("sub-command")
            .description(TextComponent.of("sub-description"))
            .build()
        val interArg = arg("intermediate", "inter-arg")
        val subCommands = subs(subCommand)
        val command = newManager().newCommand("main")
            .description(TextComponent.of("description"))
            .addParts(interArg, subCommands)
            .build()
        assertEquals("""
            description
            Usage: main <intermediate> <sub-command>
            Arguments:
              <intermediate>: inter-arg
        """.trimIndent(), TextHelper.reduceToText(HelpGenerator.create(listOf(command)).fullHelp))
        assertEquals("""
            sub-description
            Usage: main <intermediate> sub-command
        """.trimIndent(), TextHelper.reduceToText(HelpGenerator.create(listOf(command, subCommand)).fullHelp))
        assertEquals("main inter-value sub-command", TextHelper.reduceToText(HelpGenerator.create(
            TestParseResult(
                ImmutableList.of(command, subCommand),
                ImmutableList.of(
                    interArg.bind("inter-value"),
                    subCommands.bind("sub-command")
                ),
                TestCommandParameters()
            )
        ).fullName))
        assertEquals("main-alias inter-value sub-command", TextHelper.reduceToText(HelpGenerator.create(
            TestParseResult(
                ImmutableList.of(command, subCommand),
                ImmutableList.of(
                    interArg.bind("inter-value"),
                    subCommands.bind("sub-command")
                ),
                TestCommandParameters(
                    TestCommandMetadata("main-alias", ImmutableList.of("main-alias", "inter-value", "sub-command"))
                )
            )
        ).fullName))
    }

}