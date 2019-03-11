package org.enginehub.piston;

import com.google.common.base.Splitter;
import com.google.inject.Key;
import org.enginehub.piston.commands.NoArgCommand;
import org.enginehub.piston.commands.NoArgCommandRegistration;
import org.enginehub.piston.commands.NoArgWithInjectedCommand;
import org.enginehub.piston.commands.NoArgWithInjectedCommandRegistration;
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
        manager.execute(Splitter.on(' ').splitToList(config.getCommandLine()));
        config.getVerification().accept(mock);
        verifyNoMoreInteractions(mock);
    }

    @Test
    void noArgCommand() {
        testCommand(
            new TestCommandConfig<>(
                NoArgCommand.class,
                NoArgCommandRegistration::new,
                cmd -> verify(cmd).noArg())
                .setCommandLine("/no-arg")
        );
    }

    @Test
    void noArgInjectedCommand() {
        String injected = new Object().toString();
        testCommand(
            new TestCommandConfig<>(
                NoArgWithInjectedCommand.class,
                NoArgWithInjectedCommandRegistration::new,
                cmd -> verify(cmd).noArg(injected))
                .setCommandLine("/no-arg-injected")
                .setManagerSetup(commandManager -> commandManager.injectValue(
                    Key.get(String.class), () -> injected
                ))
        );
    }
}
