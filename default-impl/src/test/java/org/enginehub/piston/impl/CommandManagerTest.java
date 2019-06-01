package org.enginehub.piston.impl;

import com.google.common.collect.ImmutableList;
import net.kyori.text.TextComponent;
import net.kyori.text.TranslatableComponent;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.exception.ConditionFailedException;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.part.SubCommandPart;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("A CommandManager")
public class CommandManagerTest {

    @Test
    @DisplayName("fails if the root condition isn't satisfied when parsing")
    void parseFailsOnRootConditionUnsatisfied() {
        CommandManager manager = new CommandManagerImpl();
        manager.register("test", cmd -> {
            cmd.description(TextComponent.of("Test"))
                .condition(Command.Condition.FALSE);
        });

        assertThrows(ConditionFailedException.class, () ->
            manager.parse(InjectedValueAccess.EMPTY, ImmutableList.of("test"))
        );
    }

    @Test
    @DisplayName("fails if a sub-condition isn't satisfied when parsing")
    void parseFailsOnSubConditionUnsatisfied() {
        CommandManager manager = new CommandManagerImpl();
        manager.register("test", cmd -> {
            Command sub = manager.newCommand("sub")
                .description(TextComponent.of("Sub"))
                .condition(Command.Condition.FALSE)
                .build();
            cmd.description(TextComponent.of("Test"))
                .addPart(SubCommandPart.builder(TranslatableComponent.of("actions"), TextComponent.of("Sub-actions"))
                    .required()
                    .withCommands(ImmutableList.of(sub))
                    .build());
        });

        assertThrows(ConditionFailedException.class, () ->
            manager.parse(InjectedValueAccess.EMPTY, ImmutableList.of("test", "sub"))
        );
    }
}
