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
import java.lang.Throwable;
import java.lang.reflect.Method;
import java.util.Collection;
import net.kyori.text.TextComponent;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.gen.CommandCallListener;
import org.enginehub.piston.gen.CommandRegistration;
import org.enginehub.piston.inject.Key;

final class NonArgParametersRegistration implements CommandRegistration<NonArgParameters> {
    private static final Key<Object> object_Key = Key.of(Object.class);

    private CommandManager commandManager;

    private NonArgParameters containerInstance;

    private ImmutableList<CommandCallListener> listeners;

    private NonArgParametersRegistration() {
        this.listeners = ImmutableList.of();
    }

    static NonArgParametersRegistration builder() {
        return new NonArgParametersRegistration();
    }

    public NonArgParametersRegistration commandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
        return this;
    }

    public NonArgParametersRegistration containerInstance(NonArgParameters containerInstance) {
        this.containerInstance = containerInstance;
        return this;
    }

    public NonArgParametersRegistration listeners(Collection<CommandCallListener> listeners) {
        this.listeners = ImmutableList.copyOf(listeners);
        return this;
    }

    public void build() {
        commandManager.register("nonArgCommandParameters", b -> {
            b.aliases(ImmutableList.of());
            b.description(TextComponent.of("DESCRIPTION"));
            b.parts(ImmutableList.of());
            b.action(this::cmd$nonArgCommandParameters);
        });
        commandManager.register("nonArgInjected", b -> {
            b.aliases(ImmutableList.of());
            b.description(TextComponent.of("DESCRIPTION"));
            b.parts(ImmutableList.of());
            b.action(this::cmd$nonArgInjected);
        });
    }

    private int cmd$nonArgCommandParameters(CommandParameters parameters) {
        Method cmdMethod = getCommandMethod(NonArgParameters.class, "nonArgCommandParameters", CommandParameters.class);
        listenersBeforeCall(listeners, cmdMethod, parameters);
        try {
            int result;
            containerInstance.nonArgCommandParameters(this.extract$params(parameters));
            result = 1;
            listenersAfterCall(listeners, cmdMethod, parameters);
            return result;
        } catch (Throwable t) {
            listenersAfterThrow(listeners, cmdMethod, parameters, t);
            throw t;
        }
    }

    private int cmd$nonArgInjected(CommandParameters parameters) {
        Method cmdMethod = getCommandMethod(NonArgParameters.class, "nonArgInjected", Object.class);
        listenersBeforeCall(listeners, cmdMethod, parameters);
        try {
            int result;
            containerInstance.nonArgInjected(this.extract$injected(parameters));
            result = 1;
            listenersAfterCall(listeners, cmdMethod, parameters);
            return result;
        } catch (Throwable t) {
            listenersAfterThrow(listeners, cmdMethod, parameters, t);
            throw t;
        }
    }

    private CommandParameters extract$params(CommandParameters parameters) {
        return parameters;
    }

    private Object extract$injected(CommandParameters parameters) {
        return requireOptional(object_Key, "injected", parameters.injectedValue(object_Key));
    }
}