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

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

final class TestCommandConfig<CMD> {

    @Nullable
    private String commandLine;
    private Class<CMD> cmdClass;
    private BiConsumer<CommandManager, CMD> registration;
    private Consumer<CMD> verification;
    private Consumer<CommandManager> managerSetup = commandManager -> {
    };

    public TestCommandConfig(Class<CMD> cmdClass,
                             BiConsumer<CommandManager, CMD> registration,
                             Consumer<CMD> verification) {
        this.cmdClass = cmdClass;
        this.registration = registration;
        this.verification = verification;
    }

    public String getCommandLine() {
        return checkNotNull(commandLine);
    }

    public TestCommandConfig<CMD> setCommandLine(String commandLine) {
        this.commandLine = commandLine;
        return this;
    }

    public Class<CMD> getCmdClass() {
        return checkNotNull(cmdClass);
    }

    public TestCommandConfig<CMD> setCmdClass(Class<CMD> cmdClass) {
        this.cmdClass = cmdClass;
        return this;
    }

    public BiConsumer<CommandManager, CMD> getRegistration() {
        return checkNotNull(registration);
    }

    public TestCommandConfig<CMD> setRegistration(BiConsumer<CommandManager, CMD> registration) {
        this.registration = registration;
        return this;
    }

    public Consumer<CMD> getVerification() {
        return checkNotNull(verification);
    }

    public TestCommandConfig<CMD> setVerification(Consumer<CMD> verification) {
        this.verification = verification;
        return this;
    }

    @Nullable
    public Consumer<CommandManager> getManagerSetup() {
        return managerSetup;
    }

    public TestCommandConfig<CMD> setManagerSetup(Consumer<CommandManager> managerSetup) {
        this.managerSetup = managerSetup;
        return this;
    }
}
