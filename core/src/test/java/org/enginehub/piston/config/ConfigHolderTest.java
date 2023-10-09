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

package org.enginehub.piston.config;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@DisplayName("A ConfigHolder")
public class ConfigHolderTest {

    private static final class TestCase {
        private final String name;
        private final Component input;
        private final Component expected;

        private TestCase(String name, Component input, Component expected) {
            this.name = name;
            this.input = input;
            this.expected = expected;
        }
    }

    private final ConfigRenderer renderer = ConfigRenderer.getInstance();
    private final ConfigHolder holder = ConfigHolder.create();
    private final Component inputWithStyles = ColorConfig.helpText()
        .wrap(TextConfig.commandPrefixValue());
    private final TextComponent outputStyled = Component.text("prefix!", NamedTextColor.DARK_PURPLE);

    @BeforeEach
    void setUp() {
        holder.addConfig(ColorConfig.helpText().value(outputStyled.color()));
        holder.addConfig(TextConfig.commandPrefix().value(outputStyled.content()));
    }

    @DisplayName("replaces styled components")
    @TestFactory
    Stream<DynamicNode> test() {
        List<TestCase> testCases = ImmutableList.of(
            new TestCase("Translatable",
                Component.translatable("test", inputWithStyles),
                Component.translatable("test", outputStyled)),
            new TestCase("Text",
                Component.text("test").append(inputWithStyles),
                Component.text("test").append(outputStyled))
        );

        return testCases.stream().map(testCase ->
            dynamicTest(testCase.name + "Component", () -> {
                Component actual = renderer.render(testCase.input, holder);
                assertEquals(testCase.expected, actual);
            })
        );
    }

}
