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
import net.kyori.adventure.text.Component;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.gen.AlwaysTrueConditionGenerator;
import org.enginehub.piston.gen.CommandCallListener;
import org.enginehub.piston.gen.CommandRegistration;

final class NoArgRegistration implements CommandRegistration<NoArg> {
    private CommandManager commandManager;

    private NoArg containerInstance;

    private AlwaysTrueConditionGenerator alwaysTrueConditionGenerator;

    private ImmutableList<CommandCallListener> listeners;

    private NoArgRegistration() {
        this.listeners = ImmutableList.of();
    }

    static NoArgRegistration builder() {
        return new NoArgRegistration();
    }

    public NoArgRegistration commandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
        return this;
    }

    public NoArgRegistration containerInstance(NoArg containerInstance) {
        this.containerInstance = containerInstance;
        return this;
    }

    NoArgRegistration alwaysTrueConditionGenerator(
        AlwaysTrueConditionGenerator alwaysTrueConditionGenerator) {
        this.alwaysTrueConditionGenerator = alwaysTrueConditionGenerator;
        return this;
    }

    public NoArgRegistration listeners(Collection<CommandCallListener> listeners) {
        this.listeners = ImmutableList.copyOf(listeners);
        return this;
    }

    public void build() {
        commandManager.register("noArgument", b -> {
            b.aliases(ImmutableList.of());
            b.description(Component.text("DESCRIPTION"));
            b.parts(ImmutableList.of());
            b.action(this::cmd$noArgument);
        });
        commandManager.register("noArgumentFooter", b -> {
            b.aliases(ImmutableList.of());
            b.description(Component.text("DESCRIPTION"));
            b.footer(Component.text("DESC FOOTER"));
            b.parts(ImmutableList.of());
            b.action(this::cmd$noArgumentFooter);
        });
        commandManager.register("noArgumentCondition", b -> {
            b.aliases(ImmutableList.of());
            b.description(Component.text("DESCRIPTION"));
            b.parts(ImmutableList.of());
            b.action(this::cmd$noArgumentCondition);
            Method commandMethod = getCommandMethod(NoArg.class, "noArgCondition");
            Command.Condition condition = alwaysTrueConditionGenerator.generateCondition(commandMethod);
            b.condition(condition);
        });
        commandManager.register("noArgumentStatic", b -> {
            b.aliases(ImmutableList.of());
            b.description(Component.text("DESCRIPTION"));
            b.parts(ImmutableList.of());
            b.action(this::cmd$noArgumentStatic);
        });
    }

    private int cmd$noArgument(CommandParameters parameters) throws Exception {
        Method cmdMethod = getCommandMethod(NoArg.class, "noArg");
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

    private int cmd$noArgumentFooter(CommandParameters parameters) {
        Method cmdMethod = getCommandMethod(NoArg.class, "noArgFooter");
        listenersBeforeCall(listeners, cmdMethod, parameters);
        try {
            int result;
            containerInstance.noArgFooter();
            result = 1;
            listenersAfterCall(listeners, cmdMethod, parameters);
            return result;
        } catch (Throwable t) {
            listenersAfterThrow(listeners, cmdMethod, parameters, t);
            throw t;
        }
    }

    private int cmd$noArgumentCondition(CommandParameters parameters) {
        Method cmdMethod = getCommandMethod(NoArg.class, "noArgCondition");
        listenersBeforeCall(listeners, cmdMethod, parameters);
        try {
            int result;
            containerInstance.noArgCondition();
            result = 1;
            listenersAfterCall(listeners, cmdMethod, parameters);
            return result;
        } catch (Throwable t) {
            listenersAfterThrow(listeners, cmdMethod, parameters, t);
            throw t;
        }
    }

    private int cmd$noArgumentStatic(CommandParameters parameters) {
        Method cmdMethod = getCommandMethod(NoArg.class, "noArgStatic");
        listenersBeforeCall(listeners, cmdMethod, parameters);
        try {
            int result;
            NoArg.noArgStatic();
            result = 1;
            listenersAfterCall(listeners, cmdMethod, parameters);
            return result;
        } catch (Throwable t) {
            listenersAfterThrow(listeners, cmdMethod, parameters, t);
            throw t;
        }
    }
}
