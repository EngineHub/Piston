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

package org.enginehub.piston

import org.enginehub.piston.commands.NoArgCommand
import org.enginehub.piston.commands.NoArgCommandRegistration
import org.enginehub.piston.commands.NoArgWithInjectedCommand
import org.enginehub.piston.commands.NoArgWithInjectedCommandRegistration
import org.enginehub.piston.commands.SingleArgCommand
import org.enginehub.piston.commands.SingleArgCommandRegistration
import org.enginehub.piston.commands.SingleOptionalArgCommand
import org.enginehub.piston.commands.SingleOptionalArgCommandRegistration
import org.enginehub.piston.exception.UsageException
import org.enginehub.piston.inject.InjectedValueAccess
import org.enginehub.piston.inject.Key
import org.enginehub.piston.inject.MapBackedValueStore
import org.enginehub.piston.util.ValueProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.mockito.Mockito.verify

@Execution(ExecutionMode.CONCURRENT)
class BasicCommandTest {

    @Test
    fun noArgCommand() {
        withMockedContainer<NoArgCommand> { ci ->
            val manager = newManager().apply {
                installCommands(ci, NoArgCommandRegistration.builder())
            }

            manager.execute(InjectedValueAccess.EMPTY, listOf("no-arg"))

            verify(ci).noArg()
        }
    }

    @Test
    fun noArgInjectedCommand() {
        val injected = Any().toString()
        withMockedContainer<NoArgWithInjectedCommand> { ci ->
            val manager = newManager().apply {
                installCommands(ci, NoArgWithInjectedCommandRegistration.builder())
            }

            manager.execute(MapBackedValueStore.create(mapOf(
                    Key.of(String::class.java) to ValueProvider.constant(injected)
            )), listOf("no-arg-injected"))

            verify(ci).noArg(injected)
        }
    }

    @Test
    fun singleArgCommand() {
        val testString = "a varied argument"
        withMockedContainer<SingleArgCommand> { ci ->
            val manager = newManager().apply {
                installCommands(ci, SingleArgCommandRegistration.builder())
            }

            manager.execute(InjectedValueAccess.EMPTY, listOf("single-arg", testString))

            verify(ci).singleArg(testString)
        }
    }

    @Test
    fun singleArgCommandWithDashDash() {
        val testString = "a varied argument"
        withMockedContainer<SingleArgCommand> { ci ->
            val manager = newManager().apply {
                installCommands(ci, SingleArgCommandRegistration.builder())
            }

            manager.execute(InjectedValueAccess.EMPTY, listOf("single-arg", "--", testString))

            verify(ci).singleArg(testString)
        }
    }

    @Test
    fun notEnoughArgsForSingleArg() {
        withMockedContainer<SingleArgCommand> { ci ->
            val manager = newManager().apply {
                installCommands(ci, SingleArgCommandRegistration.builder())
            }

            val notEnoughArgEx = assertThrows<UsageException> {
                manager.execute(InjectedValueAccess.EMPTY, listOf("single-arg"))
            }

            assertEquals("Missing argument for <piston.argument.first>.", notEnoughArgEx.message)
        }
    }

    @Test
    fun singleOptionalArgCommand() {
        val testString = "a varied argument"
        withMockedContainer<SingleOptionalArgCommand> { ci ->
            val manager = newManager().apply {
                installCommands(ci, SingleOptionalArgCommandRegistration.builder())
            }

            manager.execute(InjectedValueAccess.EMPTY, listOf("single-arg-opt"))

            verify(ci).singleArg(null)
        }
        withMockedContainer<SingleOptionalArgCommand> { ci ->
            val manager = newManager().apply {
                installCommands(ci, SingleOptionalArgCommandRegistration.builder())
            }

            manager.execute(InjectedValueAccess.EMPTY, listOf("single-arg-opt", testString))

            verify(ci).singleArg(testString)
        }
    }
}
