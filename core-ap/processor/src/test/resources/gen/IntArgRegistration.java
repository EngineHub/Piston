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
import java.lang.Integer;
import java.lang.NoSuchMethodException;
import java.lang.Object;
import java.lang.RuntimeException;
import java.lang.Throwable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.gen.CommandCallListener;
import org.enginehub.piston.gen.CommandRegistration;
import org.enginehub.piston.gen.InjectAlpha;
import org.enginehub.piston.gen.InjectDelta;
import org.enginehub.piston.gen.InjectGamma;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.part.CommandArgument;

final class IntArgRegistration implements CommandRegistration<IntArg> {
    private static final Key<Integer> integer_Key = Key.of(Integer.class);
    private static final Key<Integer> integer_injectGamma_something_to_match__Key = Key.of(Integer.class, new Object() {
        Annotation a(@InjectGamma("something to match") Object ah) {
            try {
                return getClass().getDeclaredMethod("a", Object.class).getParameterAnnotations()[0][0];
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }.a(null));
    private static final Key<Integer> integer_injectDeltaQux45$Baz32$Thq1099_Key = Key.of(Integer.class, new Object() {
        Annotation a(@InjectDelta(qux = 45, baz = 32, thq = { 10, 99 }) Object ah) {
            try {
                return getClass().getDeclaredMethod("a", Object.class).getParameterAnnotations()[0][0];
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }.a(null));
    private static final Key<Integer> integer_injectAlpha_Key = Key.of(Integer.class, InjectAlpha.class);

    private CommandManager commandManager;

    private IntArg containerInstance;

    private ImmutableList<CommandCallListener> listeners;

    private final CommandArgument argPart = arg(TranslatableComponent.of("piston.argument.arg"), TextComponent.of("ARG DESCRIPTION"))
        .defaultsTo(ImmutableList.of())
        .ofTypes(ImmutableList.of(integer_Key))
        .build();

    private final CommandArgument argPart2 = arg(TranslatableComponent.of("piston.argument.arg"), TextComponent.of("ARG DESCRIPTION"))
        .defaultsTo(ImmutableList.of())
        .ofTypes(ImmutableList.of(integer_injectGamma_something_to_match__Key))
        .build();

    private final CommandArgument deltaPart = arg(TranslatableComponent.of("piston.argument.delta"), TextComponent.of("ARG DESCRIPTION"))
        .defaultsTo(ImmutableList.of())
        .ofTypes(ImmutableList.of(integer_injectDeltaQux45$Baz32$Thq1099_Key))
        .build();

    private final CommandArgument alphaPart = arg(TranslatableComponent.of("piston.argument.alpha"), TextComponent.of("ARG DESCRIPTION"))
        .defaultsTo(ImmutableList.of())
        .ofTypes(ImmutableList.of(integer_injectAlpha_Key))
        .build();

    private final CommandArgument argPart3 = arg(TranslatableComponent.of("piston.argument.args"), TextComponent.of("ARG DESCRIPTION"))
        .defaultsTo(ImmutableList.of())
        .ofTypes(ImmutableList.of(integer_Key))
        .variable(true)
        .build();

    private IntArgRegistration() {
        this.listeners = ImmutableList.of();
    }

    static IntArgRegistration builder() {
        return new IntArgRegistration();
    }

    public IntArgRegistration commandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
        return this;
    }

    public IntArgRegistration containerInstance(IntArg containerInstance) {
        this.containerInstance = containerInstance;
        return this;
    }

    public IntArgRegistration listeners(Collection<CommandCallListener> listeners) {
        this.listeners = ImmutableList.copyOf(listeners);
        return this;
    }

    public void build() {
        commandManager.register("intArgument", b -> {
            b.aliases(ImmutableList.of());
            b.description(TextComponent.of("DESCRIPTION"));
            b.parts(ImmutableList.of(argPart));
            b.action(this::cmd$intArgument);
        });
        commandManager.register("annotatedIntArgument", b -> {
            b.aliases(ImmutableList.of());
            b.description(TextComponent.of("DESCRIPTION"));
            b.parts(ImmutableList.of(argPart2));
            b.action(this::cmd$annotatedIntArgument);
        });
        commandManager.register("annotatedIntArgument2", b -> {
            b.aliases(ImmutableList.of());
            b.description(TextComponent.of("DESCRIPTION"));
            b.parts(ImmutableList.of(deltaPart));
            b.action(this::cmd$annotatedIntArgument2);
        });
        commandManager.register("annotatedIntArgument3", b -> {
            b.aliases(ImmutableList.of());
            b.description(TextComponent.of("DESCRIPTION"));
            b.parts(ImmutableList.of(alphaPart));
            b.action(this::cmd$annotatedIntArgument3);
        });
        commandManager.register("variableIntArgument", b -> {
            b.aliases(ImmutableList.of());
            b.description(TextComponent.of("DESCRIPTION"));
            b.parts(ImmutableList.of(argPart3));
            b.action(this::cmd$variableIntArgument);
        });
    }

    private int cmd$intArgument(CommandParameters parameters) {
        Method cmdMethod = getCommandMethod(IntArg.class, "intArg", int.class);
        listenersBeforeCall(listeners, cmdMethod, parameters);
        try {
            int result;
            result = containerInstance.intArg(this.extract$arg(parameters));
            listenersAfterCall(listeners, cmdMethod, parameters);
            return result;
        } catch (Throwable t) {
            listenersAfterThrow(listeners, cmdMethod, parameters, t);
            throw t;
        }
    }

    private int cmd$annotatedIntArgument(CommandParameters parameters) {
        Method cmdMethod = getCommandMethod(IntArg.class, "annotatedIntArg", int.class);
        listenersBeforeCall(listeners, cmdMethod, parameters);
        try {
            int result;
            containerInstance.annotatedIntArg(this.extract$arg2(parameters));
            result = 1;
            listenersAfterCall(listeners, cmdMethod, parameters);
            return result;
        } catch (Throwable t) {
            listenersAfterThrow(listeners, cmdMethod, parameters, t);
            throw t;
        }
    }

    private int cmd$annotatedIntArgument2(CommandParameters parameters) {
        Method cmdMethod = getCommandMethod(IntArg.class, "annotatedIntArg2", int.class);
        listenersBeforeCall(listeners, cmdMethod, parameters);
        try {
            int result;
            containerInstance.annotatedIntArg2(this.extract$delta(parameters));
            result = 1;
            listenersAfterCall(listeners, cmdMethod, parameters);
            return result;
        } catch (Throwable t) {
            listenersAfterThrow(listeners, cmdMethod, parameters, t);
            throw t;
        }
    }

    private int cmd$annotatedIntArgument3(CommandParameters parameters) {
        Method cmdMethod = getCommandMethod(IntArg.class, "annotatedIntArg3", int.class);
        listenersBeforeCall(listeners, cmdMethod, parameters);
        try {
            int result;
            containerInstance.annotatedIntArg3(this.extract$alpha(parameters));
            result = 1;
            listenersAfterCall(listeners, cmdMethod, parameters);
            return result;
        } catch (Throwable t) {
            listenersAfterThrow(listeners, cmdMethod, parameters, t);
            throw t;
        }
    }

    private int cmd$variableIntArgument(CommandParameters parameters) {
        Method cmdMethod = getCommandMethod(IntArg.class, "variableIntArg", List.class);
        listenersBeforeCall(listeners, cmdMethod, parameters);
        try {
            int result;
            containerInstance.variableIntArg(this.extract$arg3(parameters));
            result = 1;
            listenersAfterCall(listeners, cmdMethod, parameters);
            return result;
        } catch (Throwable t) {
            listenersAfterThrow(listeners, cmdMethod, parameters, t);
            throw t;
        }
    }

    private int extract$arg(CommandParameters parameters) {
        return argPart.value(parameters).asSingle(integer_Key);
    }

    private int extract$arg2(CommandParameters parameters) {
        return argPart2.value(parameters).asSingle(integer_injectGamma_something_to_match__Key);
    }

    private int extract$delta(CommandParameters parameters) {
        return deltaPart.value(parameters).asSingle(integer_injectDeltaQux45$Baz32$Thq1099_Key);
    }

    private int extract$alpha(CommandParameters parameters) {
        return alphaPart.value(parameters).asSingle(integer_injectAlpha_Key);
    }

    private List<Integer> extract$arg3(CommandParameters parameters) {
        return argPart3.value(parameters).asMultiple(integer_Key);
    }
}
