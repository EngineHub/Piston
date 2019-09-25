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
import java.lang.String;
import java.lang.Throwable;
import java.lang.reflect.Method;
import java.util.Collection;
import net.kyori.text.TextComponent;
import net.kyori.text.TranslatableComponent;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.gen.CommandCallListener;
import org.enginehub.piston.gen.CommandRegistration;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.part.CommandArgument;

final class StringArgRegistration implements CommandRegistration<StringArg> {
    private static final Key<String> string_Key = Key.of(String.class);

    private CommandManager commandManager;

    private StringArg containerInstance;

    private ImmutableList<CommandCallListener> listeners;

    private final CommandArgument argPart = arg(TranslatableComponent.of("piston.argument.arg"), TextComponent.of("ARG DESCRIPTION"))
        .defaultsTo(ImmutableList.of())
        .ofTypes(ImmutableList.of(string_Key))
        .build();

    private StringArgRegistration() {
        this.listeners = ImmutableList.of();
    }

    static StringArgRegistration builder() {
        return new StringArgRegistration();
    }

    public StringArgRegistration commandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
        return this;
    }

    public StringArgRegistration containerInstance(StringArg containerInstance) {
        this.containerInstance = containerInstance;
        return this;
    }

    public StringArgRegistration listeners(Collection<CommandCallListener> listeners) {
        this.listeners = ImmutableList.copyOf(listeners);
        return this;
    }

    public void build() {
        commandManager.register("stringArgument", b -> {
            b.aliases(ImmutableList.of());
            b.description(TextComponent.of("DESCRIPTION"));
            b.parts(ImmutableList.of(argPart));
            b.action(this::stringArgument);
        });
    }

    private int stringArgument(CommandParameters parameters) {
        Method cmdMethod = getCommandMethod(StringArg.class, "stringArg", String.class);
        listenersBeforeCall(listeners, cmdMethod, parameters);
        try {
            int result;
            containerInstance.stringArg(this.extract$arg(parameters));
            result = 1;
            listenersAfterCall(listeners, cmdMethod, parameters);
            return result;
        } catch (Throwable t) {
            listenersAfterThrow(listeners, cmdMethod, parameters, t);
            throw t;
        }
    }

    private String extract$arg(CommandParameters parameters) {
        return argPart.value(parameters).asSingle(string_Key);
    }
}
