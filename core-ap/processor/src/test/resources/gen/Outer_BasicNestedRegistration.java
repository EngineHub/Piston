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

package eh;

import static org.enginehub.piston.internal.RegistrationUtil.getCommandMethod;
import static org.enginehub.piston.internal.RegistrationUtil.listenersAfterCall;
import static org.enginehub.piston.internal.RegistrationUtil.listenersAfterThrow;
import static org.enginehub.piston.internal.RegistrationUtil.listenersBeforeCall;
import static org.enginehub.piston.internal.RegistrationUtil.requireOptional;
import static org.enginehub.piston.part.CommandParts.arg;
import static org.enginehub.piston.part.CommandParts.flag;

import com.google.common.collect.ImmutableList;
import java.lang.Exception;
import java.lang.Throwable;
import java.lang.reflect.Method;
import java.util.Collection;
import net.kyori.adventure.text.TextComponent;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.gen.CommandCallListener;
import org.enginehub.piston.gen.CommandRegistration;

final class Outer_BasicNestedRegistration implements CommandRegistration<Outer.BasicNested> {
    private CommandManager commandManager;

    private Outer.BasicNested containerInstance;

    private ImmutableList<CommandCallListener> listeners;

    private Outer_BasicNestedRegistration() {
        this.listeners = ImmutableList.of();
    }

    static Outer_BasicNestedRegistration builder() {
        return new Outer_BasicNestedRegistration();
    }

    public Outer_BasicNestedRegistration commandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
        return this;
    }

    public Outer_BasicNestedRegistration containerInstance(Outer.BasicNested containerInstance) {
        this.containerInstance = containerInstance;
        return this;
    }

    public Outer_BasicNestedRegistration listeners(Collection<CommandCallListener> listeners) {
        this.listeners = ImmutableList.copyOf(listeners);
        return this;
    }

    public void build() {
        commandManager.register("noArgument", b -> {
            b.aliases(ImmutableList.of());
            b.description(TextComponent.of("DESCRIPTION"));
            b.parts(ImmutableList.of());
            b.action(this::cmd$noArgument);
        });
    }

    private int cmd$noArgument(CommandParameters parameters) throws Exception {
        Method cmdMethod = getCommandMethod(Outer.BasicNested.class, "noArg");
        listenersBeforeCall(listeners, cmdMethod, parameters);
        try {
            int result;
            containerInstance.noArg();
            result = 1;
            listenersAfterCall(listeners, cmdMethod, parameters);
            return result;
        } catch (Throwable t) {
            listenersAfterThrow(listeners, cmdMethod, parameters, t);
            throw t;
        }
    }
}
