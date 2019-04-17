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

package org.enginehub.piston;

import com.google.common.base.Splitter;
import com.google.inject.Key;
import org.enginehub.piston.commands.NoArgCommand;
import org.enginehub.piston.commands.NoArgCommandRegistration;
import org.enginehub.piston.commands.NoArgWithInjectedCommand;
import org.enginehub.piston.commands.NoArgWithInjectedCommandRegistration;
import org.enginehub.piston.commands.SingleArgCommand;
import org.enginehub.piston.commands.SingleArgCommandRegistration;
import org.enginehub.piston.commands.SingleOptionalArgCommand;
import org.enginehub.piston.commands.SingleOptionalArgCommandRegistration;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class BasicCommandTest {

    private <CMD> void testCommand(TestCommandConfig<CMD> config) {
        CMD mock = mock(config.getCmdClass());
        CommandManager manager = DefaultCommandManagerService.getInstance().newCommandManager();
        Optional.ofNullable(config.getManagerSetup())
            .ifPresent(setup -> setup.accept(manager));
        config.getRegistration().accept(manager, mock);
        manager.execute(
            InjectedValueAccess.EMPTY,
            Splitter.on(' ').splitToList(config.getCommandLine())
        );
        config.getVerification().accept(mock);
        verifyNoMoreInteractions(mock);
    }

    @Test
    void noArgCommand() {
        testCommand(
            new TestCommandConfig<>(
                NoArgCommand.class,
                TestCommandConfig.ezRegister(NoArgCommandRegistration.builder()),
                cmd -> verify(cmd).noArg())
                .setCommandLine("no-arg")
        );
    }

    @Test
    void noArgInjectedCommand() {
        String injected = new Object().toString();
        testCommand(
            new TestCommandConfig<>(
                NoArgWithInjectedCommand.class,
                TestCommandConfig.ezRegister(NoArgWithInjectedCommandRegistration.builder()),
                cmd -> verify(cmd).noArg(injected))
                .setCommandLine("no-arg-injected")
                .setManagerSetup(commandManager -> commandManager.injectValue(
                    Key.get(String.class), access -> Optional.of(injected)
                ))
        );
    }

    @Test
    void singleArgCommand() {
        String testString = "somethingnotspaced";
        testCommand(
            new TestCommandConfig<>(
                SingleArgCommand.class,
                TestCommandConfig.ezRegister(SingleArgCommandRegistration.builder()),
                cmd -> verify(cmd).singleArg(testString))
                .setCommandLine("single-arg " + testString)
        );
    }

    @Test
    void singleOptionalArgCommand() {
        String testString = "somethingnotspaced";
        testCommand(
            new TestCommandConfig<>(
                SingleOptionalArgCommand.class,
                TestCommandConfig.ezRegister(SingleOptionalArgCommandRegistration.builder()),
                cmd -> verify(cmd).singleArg(""))
                .setCommandLine("single-arg-opt")
        );
        testCommand(
            new TestCommandConfig<>(
                SingleOptionalArgCommand.class,
                TestCommandConfig.ezRegister(SingleOptionalArgCommandRegistration.builder()),
                cmd -> verify(cmd).singleArg(testString))
                .setCommandLine("single-arg-opt " + testString)
        );
    }
}
