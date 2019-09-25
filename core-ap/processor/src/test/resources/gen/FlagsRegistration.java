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
import org.enginehub.piston.part.ArgAcceptingCommandFlag;
import org.enginehub.piston.part.NoArgCommandFlag;

final class FlagsRegistration implements CommandRegistration<Flags> {
    private static final Key<String> string_Key = Key.of(String.class);
    private CommandManager commandManager;

    private Flags containerInstance;

    private ImmutableList<CommandCallListener> listeners;

    private final NoArgCommandFlag flagPart = flag('f', TextComponent.of("ARG DESCRIPTION")).build();

    private final ArgAcceptingCommandFlag flagPart2 = flag('f', TextComponent.of("ARG DESCRIPTION"))
        .withRequiredArg()
        .argNamed(TranslatableComponent.of("piston.argument.flag"))
        .defaultsTo(ImmutableList.of("DEFAULT"))
        .ofTypes(ImmutableList.of(string_Key))
        .build();

    private final ArgAcceptingCommandFlag flagPart3 = flag('f', TextComponent.of("ARG DESCRIPTION"))
        .withRequiredArg()
        .argNamed(TranslatableComponent.of("piston.argument.ARG NAME"))
        .defaultsTo(ImmutableList.of("DEFAULT"))
        .ofTypes(ImmutableList.of(string_Key))
        .build();

    private FlagsRegistration() {
        this.listeners = ImmutableList.of();
    }

    static FlagsRegistration builder() {
        return new FlagsRegistration();
    }

    public FlagsRegistration commandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
        return this;
    }

    public FlagsRegistration containerInstance(Flags containerInstance) {
        this.containerInstance = containerInstance;
        return this;
    }

    public FlagsRegistration listeners(Collection<CommandCallListener> listeners) {
        this.listeners = ImmutableList.copyOf(listeners);
        return this;
    }

    public void build() {
        commandManager.register("booleanFlag", b -> {
            b.aliases(ImmutableList.of());
            b.description(TextComponent.of("DESCRIPTION"));
            b.parts(ImmutableList.of(flagPart));
            b.action(this::cmd$booleanFlag);
        });
        commandManager.register("stringArgFlag", b -> {
            b.aliases(ImmutableList.of());
            b.description(TextComponent.of("DESCRIPTION"));
            b.parts(ImmutableList.of(flagPart2));
            b.action(this::cmd$stringArgFlag);
        });
        commandManager.register("stringArgFlagCustom", b -> {
            b.aliases(ImmutableList.of());
            b.description(TextComponent.of("DESCRIPTION"));
            b.parts(ImmutableList.of(flagPart3));
            b.action(this::cmd$stringArgFlagCustom);
        });
    }

    private int cmd$booleanFlag(CommandParameters parameters) {
        Method cmdMethod = getCommandMethod(Flags.class, "booleanFlag", boolean.class);
        listenersBeforeCall(listeners, cmdMethod, parameters);
        try {
            int result;
            containerInstance.booleanFlag(this.extract$flag(parameters));
            result = 1;
            listenersAfterCall(listeners, cmdMethod, parameters);
            return result;
        } catch (Throwable t) {
            listenersAfterThrow(listeners, cmdMethod, parameters, t);
            throw t;
        }
    }

    private int cmd$stringArgFlag(CommandParameters parameters) {
        Method cmdMethod = getCommandMethod(Flags.class, "stringArgFlag", String.class);
        listenersBeforeCall(listeners, cmdMethod, parameters);
        try {
            int result;
            containerInstance.stringArgFlag(this.extract$flag2(parameters));
            result = 1;
            listenersAfterCall(listeners, cmdMethod, parameters);
            return result;
        } catch (Throwable t) {
            listenersAfterThrow(listeners, cmdMethod, parameters, t);
            throw t;
        }
    }

    private int cmd$stringArgFlagCustom(CommandParameters parameters) {
        Method cmdMethod = getCommandMethod(Flags.class, "stringArgFlagCustom", String.class);
        listenersBeforeCall(listeners, cmdMethod, parameters);
        try {
            int result;
            containerInstance.stringArgFlagCustom(this.extract$flag3(parameters));
            result = 1;
            listenersAfterCall(listeners, cmdMethod, parameters);
            return result;
        } catch (Throwable t) {
            listenersAfterThrow(listeners, cmdMethod, parameters, t);
            throw t;
        }
    }

    private boolean extract$flag(CommandParameters parameters) {
        return flagPart.in(parameters);
    }

    private String extract$flag2(CommandParameters parameters) {
        return flagPart2.value(parameters).asSingle(string_Key);
    }

    private String extract$flag3(CommandParameters parameters) {
        return flagPart3.value(parameters).asSingle(string_Key);
    }
}