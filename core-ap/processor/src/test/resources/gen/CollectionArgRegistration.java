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
import java.lang.Object;
import java.lang.String;
import java.lang.Throwable;
import java.lang.reflect.Method;
import java.util.Collection;
import net.kyori.adventure.text.Component;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.gen.CommandCallListener;
import org.enginehub.piston.gen.CommandRegistration;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.part.CommandArgument;

final class CollectionArgRegistration implements CommandRegistration<CollectionArg> {
    private static final Key<String> string_Key = Key.of(String.class);
    private static final Key<Object> object_Key = Key.of(Object.class);

    private CommandManager commandManager;

    private CollectionArg containerInstance;

    private ImmutableList<CommandCallListener> listeners;

    private final CommandArgument argPart = arg(Component.translatable("piston.argument.arg"), Component.text("ARG DESCRIPTION"))
        .defaultsTo(ImmutableList.of())
        .ofTypes(ImmutableList.of(string_Key))
        .build();

    private final CommandArgument argPart2 = arg(Component.translatable("piston.argument.arg"), Component.text("ARG DESCRIPTION"))
        .defaultsTo(ImmutableList.of())
        .ofTypes(ImmutableList.of(object_Key))
        .build();

    private CollectionArgRegistration() {
        this.listeners = ImmutableList.of();
    }

    static CollectionArgRegistration builder() {
        return new CollectionArgRegistration();
    }

    public CollectionArgRegistration commandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
        return this;
    }

    public CollectionArgRegistration containerInstance(CollectionArg containerInstance) {
        this.containerInstance = containerInstance;
        return this;
    }

    public CollectionArgRegistration listeners(Collection<CommandCallListener> listeners) {
        this.listeners = ImmutableList.copyOf(listeners);
        return this;
    }

    public void build() {
        commandManager.register("collectionArgument", b -> {
            b.aliases(ImmutableList.of());
            b.description(Component.text("DESCRIPTION"));
            b.parts(ImmutableList.of(argPart));
            b.action(this::cmd$collectionArgument);
        });
        commandManager.register("objectArgument", b -> {
            b.aliases(ImmutableList.of());
            b.description(Component.text("DESCRIPTION"));
            b.parts(ImmutableList.of(argPart2));
            b.action(this::cmd$objectArgument);
        });
    }

    private int cmd$collectionArgument(CommandParameters parameters) {
        Method cmdMethod = getCommandMethod(CollectionArg.class, "collectionArg", Collection.class);
        listenersBeforeCall(listeners, cmdMethod, parameters);
        try {
            int result;
            containerInstance.collectionArg(this.extract$arg(parameters));
            result = 1;
            listenersAfterCall(listeners, cmdMethod, parameters);
            return result;
        } catch (Throwable t) {
            listenersAfterThrow(listeners, cmdMethod, parameters, t);
            throw t;
        }
    }

    private int cmd$objectArgument(CommandParameters parameters) {
        Method cmdMethod = getCommandMethod(CollectionArg.class, "objectArg", Object.class);
        listenersBeforeCall(listeners, cmdMethod, parameters);
        try {
            int result;
            containerInstance.objectArg(this.extract$arg2(parameters));
            result = 1;
            listenersAfterCall(listeners, cmdMethod, parameters);
            return result;
        } catch (Throwable t) {
            listenersAfterThrow(listeners, cmdMethod, parameters, t);
            throw t;
        }
    }

    private Collection<String> extract$arg(CommandParameters parameters) {
        return argPart.value(parameters).asMultiple(string_Key);
    }

    private Object extract$arg2(CommandParameters parameters) {
        return argPart2.value(parameters).asSingle(object_Key);
    }
}
