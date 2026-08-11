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

package org.enginehub.piston.impl;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.FailedConversion;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.exception.ConditionFailedException;
import org.enginehub.piston.exception.ConversionFailedException;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.SubCommandPart;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("A CommandManager")
public class CommandManagerTest {

    @Test
    @DisplayName("fails if the root condition isn't satisfied when parsing")
    void parseFailsOnRootConditionUnsatisfied() {
        CommandManager manager = new CommandManagerImpl();
        manager.register("test", cmd -> {
            cmd.description(Component.text("Test"))
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
                .description(Component.text("Sub"))
                .condition(Command.Condition.FALSE)
                .build();
            cmd.description(Component.text("Test"))
                .addPart(SubCommandPart.builder(Component.translatable("actions"), Component.text("Sub-actions"))
                    .required()
                    .withCommands(ImmutableList.of(sub))
                    .build());
        });

        assertThrows(ConditionFailedException.class, () ->
            manager.parse(InjectedValueAccess.EMPTY, ImmutableList.of("test", "sub"))
        );
    }

    @Test
    @DisplayName("reuses a rejected converter result when reporting the parse failure")
    void parseReusesRejectedConverterResult() {
        CommandManager manager = new CommandManagerImpl();
        Key<ChangingConversion> key = Key.of(ChangingConversion.class);
        AtomicInteger conversionCount = new AtomicInteger();
        FailedConversion<ChangingConversion> rejected = FailedConversion.from(
            new IllegalArgumentException("rejected on first conversion")
        );
        manager.registerConverter(key, new ArgumentConverter<>() {
            @Override
            public ConversionResult<ChangingConversion> convert(
                    String argument,
                    InjectedValueAccess context
            ) {
                if (conversionCount.getAndIncrement() == 0) {
                    return rejected;
                }
                return SuccessfulConversion.fromSingle(new ChangingConversion());
            }

            @Override
            public Component describeAcceptableArguments() {
                return Component.text("a changing value");
            }
        });
        manager.register("test", command -> command
            .description(Component.text("Test"))
            .addPart(CommandArgument.builder(
                Component.translatable("value"),
                Component.text("A changing value")
            ).ofTypes(ImmutableList.of(key)).build()));

        ConversionFailedException exception = assertThrows(ConversionFailedException.class, () ->
            manager.parse(InjectedValueAccess.EMPTY, ImmutableList.of("test", "value"))
        );

        assertSame(rejected, exception.getConversion());
        assertEquals(1, conversionCount.get());
    }

    private static final class ChangingConversion {
    }

}
