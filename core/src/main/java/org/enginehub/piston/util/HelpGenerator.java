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

package org.enginehub.piston.util;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.enginehub.piston.ArgBinding;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandMetadata;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.CommandParseResult;
import org.enginehub.piston.NoInputCommandParameters;
import org.enginehub.piston.config.ColorConfig;
import org.enginehub.piston.config.TextConfig;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.part.ArgAcceptingCommandFlag;
import org.enginehub.piston.part.ArgAcceptingCommandPart;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.CommandFlag;
import org.enginehub.piston.part.CommandPart;
import org.enginehub.piston.part.SubCommandPart;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.TextComponent.newline;
import static net.kyori.adventure.text.TextComponent.space;

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
        TextComponent.Builder usage = TextComponent.builder();

        String name = parseResult.getExecutionPath().get(0).getName();
        CommandMetadata metadata = parseResult.getParameters().getMetadata();
        if (metadata != null) {
            name = metadata.getCalledName();
        }
        usage.append(ColorConfig.mainText().wrap(
            TextConfig.commandPrefixValue(),
            TextComponent.of(name)
        ));

        for (String input : parseResult.getOriginalArguments()) {
            usage.append(space());
            usage.append(ColorConfig.mainText().wrap(input));
        }

        return usage.build();
    }

    /**
     * Generate a usage help text.
     */
    public Component getUsage() {
        TextComponent.Builder usage = TextComponent.builder()
            .append(ColorConfig.mainText().wrap(
                TextConfig.commandPrefixValue()
            ));

        for (Iterator<Command> iterator = parseResult.getExecutionPath().iterator(); iterator.hasNext(); ) {
            Command command = iterator.next();
            String name = command.getName();
            if (command == parseResult.getExecutionPath().get(0)) {
                CommandMetadata metadata = parseResult.getParameters().getMetadata();
                if (metadata != null) {
                    name = metadata.getCalledName();
                }
            }
            usage.append(ColorConfig.mainText().wrap(name));
            List<CommandPart> reducedParts;
            if (iterator.hasNext()) {
                // drop once we hit the sub-command part
                ImmutableList.Builder<CommandPart> parts = ImmutableList.builder();
                for (CommandPart part : command.getParts()) {
                    if (part instanceof SubCommandPart) {
                        break;
                    }
                    parts.add(part);
                }
                reducedParts = parts.build();
            } else {
                reducedParts = command.getParts();
            }
            // append a space before parts, if needed
            if (!reducedParts.isEmpty()) {
                usage.append(space());
            }
            PartHelper.appendUsage(reducedParts.stream(), usage);
            // append a space after parts/command, if needed
            if (iterator.hasNext()) {
                usage.append(space());
            }
        }

        return usage.build();
    }

    public Component getFullHelp() {
        Command primary = parseResult.getPrimaryCommand();
        ImmutableList.Builder<Component> builder = ImmutableList.builder();

        builder.add(primary.getDescription());

        builder.add(TextComponent.of("\nUsage: "));

        builder.add(getUsage());

        appendArguments(builder);

        appendFlags(builder);

        primary.getFooter().ifPresent(footer -> builder.add(newline()).add(footer));

        return ColorConfig.helpText().wrap(builder.build());
    }

    private void appendArguments(ImmutableList.Builder<Component> builder) {
        Command primary = parseResult.getPrimaryCommand();
        List<CommandArgument> args = primary.getParts().stream()
            .filter(x -> x instanceof CommandArgument)
            .map(x -> (CommandArgument) x)
            .collect(Collectors.toList());
        if (args.size() > 0) {
            builder.add(newline());
            builder.add(TextComponent.of("Arguments:\n"));
            for (Iterator<CommandArgument> iterator = args.iterator(); iterator.hasNext(); ) {
                CommandArgument arg = iterator.next();
                builder.add(TextComponent.of("  ")).add(arg.getTextRepresentation());
                addDefaultInfo(builder, arg);
                builder.add(TextComponent.of(": "))
                    .add(arg.getDescription());
                if (iterator.hasNext()) {
                    builder.add(newline());
                }
            }
        }
    }

    private void appendFlags(ImmutableList.Builder<Component> builder) {
        Command primary = parseResult.getPrimaryCommand();
        List<CommandFlag> flags = primary.getParts().stream()
            .filter(x -> x instanceof CommandFlag)
            .map(x -> (CommandFlag) x)
            .collect(Collectors.toList());
        if (flags.size() > 0) {
            builder.add(newline());
            builder.add(TextComponent.of("Flags:\n"));
            for (Iterator<CommandFlag> iterator = flags.iterator(); iterator.hasNext(); ) {
                CommandFlag flag = iterator.next();
                // produces text like "-f: Some description"
                builder.add(ColorConfig.mainText().wrap("  -" + flag.getName()));
                if (flag instanceof ArgAcceptingCommandFlag) {
                    addDefaultInfo(builder, (ArgAcceptingCommandFlag) flag);
                }
                builder.add(TextComponent.of(": "))
                    .add(flag.getDescription());
                if (iterator.hasNext()) {
                    builder.add(newline());
                }
            }
        }
    }

    private void addDefaultInfo(ImmutableList.Builder<Component> builder, ArgAcceptingCommandPart arg) {
        if (arg.getDefaults().isEmpty()) {
            return;
        }
        builder.add(TextComponent.of(" (defaults to "));
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
        builder.add(TextComponent.of(value));
        builder.add(TextComponent.of(")"));
    }

}
