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

import org.enginehub.piston.gen.CommandRegistration
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoMoreInteractions

fun newManager(): CommandManager = DefaultCommandManagerService.getInstance().newCommandManager()

fun <CI> CommandManager.installCommands(containerInstance: CI,
                                        containerRegistration: CommandRegistration<CI>) {
    containerRegistration
            .commandManager(this)
            .containerInstance(containerInstance)
            .build()
}

inline fun <CI, R> withMockedContainer(containerClass: Class<CI>, block: (CI) -> R): R {
    val mock = mock(containerClass)
    try {
        return block(mock)
    } finally {
        verifyNoMoreInteractions(mock)
    }
}
