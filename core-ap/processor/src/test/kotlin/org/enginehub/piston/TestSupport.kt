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

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import net.kyori.text.TextComponent
import net.kyori.text.TranslatableComponent
import org.enginehub.piston.gen.CommandRegistration
import org.enginehub.piston.part.ArgAcceptingCommandFlag
import org.enginehub.piston.part.CommandArgument
import org.enginehub.piston.part.CommandPart
import org.enginehub.piston.part.CommandParts
import org.enginehub.piston.part.NoArgCommandFlag
import org.enginehub.piston.part.SubCommandPart
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoMoreInteractions

fun newManager(): CommandManager = DefaultCommandManagerService.getInstance().newCommandManager()

fun <CI : Any> CommandManager.installCommands(containerInstance: CI,
                                              containerRegistration: CommandRegistration<CI>) {
    containerRegistration
        .commandManager(this)
        .containerInstance(containerInstance)
        .build()
}

inline fun <reified CI> withMockedContainer(block: (CI) -> Unit) {
    val mock = mock<CI>()
    try {
        return block(mock)
    } finally {
        verifyNoMoreInteractions(mock)
    }
}

inline fun arg(name: String, desc: String, block: CommandArgument.Builder.() -> Unit = {}): CommandArgument =
    CommandParts.arg(
        TranslatableComponent.of(name),
        TextComponent.of(desc)
    ).also(block).build()

inline fun flag(name: Char, desc: String, block: NoArgCommandFlag.Builder.() -> Unit = {}): NoArgCommandFlag =
    CommandParts.flag(
        name,
        TextComponent.of(desc)
    ).also(block).build()

inline fun argFlag(name: Char, desc: String, argName: String,
                   block: ArgAcceptingCommandFlag.Builder.() -> Unit = {}): ArgAcceptingCommandFlag =
    CommandParts.flag(
        name,
        TextComponent.of(desc)
    )
        .withRequiredArg()
        .argNamed(argName)
        .also(block).build()

fun subs(vararg subCommands: Command, required: Boolean = true): SubCommandPart =
    SubCommandPart.builder(
        TranslatableComponent.of("actions"),
        TextComponent.of("Sub-actions")
    ).run {
        withCommands(ImmutableList.copyOf(subCommands))

        if (required) required() else optional()

        build()
    }

class TestParseResult(
    private val executionPath: ImmutableList<Command>,
    private val boundArguments: ImmutableList<ArgBinding>,
    private val commandParameters: CommandParameters
) : CommandParseResult {
    override fun getExecutionPath() = executionPath

    override fun getBoundArguments() = boundArguments

    override fun getParameters() = commandParameters
}

class TestArgBinding(
    private val input: String,
    private val commandPart: CommandPart
) : ArgBinding {
    override fun getInput() = input

    override fun getParts() = ImmutableSet.of(commandPart)
}

fun CommandPart.bind(input: String) = TestArgBinding(input, this)

class TestCommandParameters(
    private val metadata: CommandMetadata? = null
) : CommandParameters by NoInputCommandParameters.builder().build() {
    override fun getMetadata() = metadata
}

class TestCommandMetadata(
    private val calledName: String,
    private val arguments: ImmutableList<String>
) : CommandMetadata {
    override fun getCalledName() = calledName

    override fun getArguments() = arguments
}
