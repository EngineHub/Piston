/*
 * Piston, a flexible command management system.
 * Copyright (C) EngineHub <http://www.enginehub.com>
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

package org.enginehub.piston.util;

import com.google.common.collect.ImmutableList;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import org.enginehub.piston.ArgBinding;
import org.enginehub.piston.ColorConfig;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandMetadata;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.CommandParseResult;
import org.enginehub.piston.NoInputCommandParameters;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.CommandFlag;
import org.enginehub.piston.part.CommandPart;
import org.enginehub.piston.part.SubCommandPart;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.kyori.text.Component.newline;
import static net.kyori.text.Component.space;

public class HelpGenerator {

    public static HelpGenerator create(Iterable<Command> commands) {
        ImmutableList<Command> executionPath = ImmutableList.copyOf(commands);
        return create(new CommandParseResult() {
            @Override
            public ImmutableList<Command> getExecutionPath() {
                return executionPath;
            }

            @Override
            public ImmutableList<ArgBinding> getBoundArguments() {
                return ImmutableList.of();
            }

            @Override
            public CommandParameters getParameters() {
                return NoInputCommandParameters.builder()
                    .injectedValues(InjectedValueAccess.EMPTY)
                    .build();
            }
        });
    }

    public static HelpGenerator create(CommandParseResult parseResult) {
        return new HelpGenerator(parseResult);
    }

    private final CommandParseResult parseResult;

    private HelpGenerator(CommandParseResult parseResult) {
        this.parseResult = parseResult;
    }

    /**
     * Generate a name for the set of commands as a whole.
     */
    public Component getFullName() {
        TextComponent.Builder usage = TextComponent.builder("");

        usage.append(TextComponent.of(parseResult.getExecutionPath().get(0).getName(), ColorConfig.getMainText()));

        for (String input : parseResult.getOriginalArguments()) {
            usage.append(space());
            usage.append(TextComponent.of(input, ColorConfig.getMainText()));
        }

        return usage.build();
    }

    /**
     * Generate a usage help text.
     */
    public Component getUsage() {
        TextComponent.Builder usage = TextComponent.builder("");

        for (Iterator<Command> iterator = parseResult.getExecutionPath().iterator(); iterator.hasNext(); ) {
            Command command = iterator.next();
            String name = command.getName();
            if (command == parseResult.getExecutionPath().get(0)) {
                CommandMetadata metadata = parseResult.getParameters().getMetadata();
                if (metadata != null) {
                    name = metadata.getCalledName();
                }
            }
            usage.append(TextComponent.of(name, ColorConfig.getMainText())).append(space());
            Stream<CommandPart> parts = command.getParts().stream();
            if (iterator.hasNext()) {
                // drop the sub-command part
                parts = parts.filter(x -> !(x instanceof SubCommandPart));
            }
            PartHelper.appendUsage(parts, usage);
            if (iterator.hasNext()) {
                usage.append(space());
            }
        }

        return usage.build();
    }

    public Component getFullHelp() {
        Command primary = parseResult.getPrimaryCommand();
        TextComponent.Builder builder = TextComponent.builder("")
            .color(ColorConfig.getHelpText());

        builder.append(primary.getDescription());

        builder.append(TextComponent.of("\nUsage: "));

        builder.append(getUsage());
        builder.append(newline());

        appendArguments(builder);

        appendFlags(builder);

        primary.getFooter().ifPresent(footer -> builder.append(footer).append(newline()));

        return builder.build();
    }

    private void appendArguments(TextComponent.Builder builder) {
        Command primary = parseResult.getPrimaryCommand();
        List<CommandArgument> args = primary.getParts().stream()
            .filter(x -> x instanceof CommandArgument)
            .map(x -> (CommandArgument) x)
            .collect(Collectors.toList());
        if (args.size() > 0) {
            builder.append(TextComponent.of("Arguments:\n"));
            for (CommandArgument arg : args) {
                builder.append(TextComponent.of("  ")).append(arg.getTextRepresentation());
                if (arg.getDefaults().size() > 0) {
                    builder.append(TextComponent.of(" (defaults to "));
                    String value;
                    if (arg.getDefaults().size() == 1) {
                        value = arg.getDefaults().get(0);
                        if (value.trim().isEmpty()) {
                            value = "none";
                        }
                    } else {
                        value = arg.getDefaults().stream()
                            .filter(s -> s.trim().length() > 0)
                            .collect(Collectors.joining(", ", "[", "]"));
                    }
                    builder.append(TextComponent.of(value));
                    builder.append(TextComponent.of(")"));
                }
                builder.append(TextComponent.of(": "))
                    .append(arg.getDescription())
                    .append(newline());
            }
        }
    }

    private void appendFlags(TextComponent.Builder builder) {
        Command primary = parseResult.getPrimaryCommand();
        List<CommandFlag> flags = primary.getParts().stream()
            .filter(x -> x instanceof CommandFlag)
            .map(x -> (CommandFlag) x)
            .collect(Collectors.toList());
        if (flags.size() > 0) {
            builder.append(TextComponent.of("Flags:\n"));
            for (CommandFlag flag : flags) {
                // produces text like "-f: Some description"
                builder.append(TextComponent.of("  -" + flag.getName(), ColorConfig.getMainText()))
                    .append(TextComponent.of(": "))
                    .append(flag.getDescription())
                    .append(newline());
            }
        }
    }

}
