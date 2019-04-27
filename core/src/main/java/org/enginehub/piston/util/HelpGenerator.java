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
import com.google.common.collect.Iterables;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import org.enginehub.piston.ColorConfig;
import org.enginehub.piston.Command;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.CommandFlag;
import org.enginehub.piston.part.CommandPart;
import org.enginehub.piston.part.SubCommandPart;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.kyori.text.Component.newline;
import static net.kyori.text.Component.space;

public class HelpGenerator {

    public static HelpGenerator create(Collection<Command> executionPath) {
        return new HelpGenerator(ImmutableList.copyOf(executionPath));
    }

    private final ImmutableList<Command> executionPath;

    private HelpGenerator(ImmutableList<Command> executionPath) {
        this.executionPath = executionPath;
    }

    /**
     * Generate a name for the set of commands as a whole.
     */
    public Component getFullName() {
        TextComponent.Builder usage = TextComponent.builder("");

        for (Iterator<Command> iterator = executionPath.iterator(); iterator.hasNext(); ) {
            Command command = iterator.next();
            usage.append(TextComponent.of(command.getName(), ColorConfig.getMainText())).append(space());
            if (iterator.hasNext()) {
                // drop the sub-command part
                Stream<CommandPart> parts = command.getParts().stream();
                parts = parts.filter(x -> !(x instanceof SubCommandPart));
                PartHelper.appendUsage(parts, usage);
            }
        }

        return usage.build();
    }

    /**
     * Generate a usage help text.
     */
    public Component getUsage() {
        TextComponent.Builder usage = TextComponent.builder("");

        for (Iterator<Command> iterator = executionPath.iterator(); iterator.hasNext(); ) {
            Command command = iterator.next();
            usage.append(TextComponent.of(command.getName(), ColorConfig.getMainText())).append(space());
            Stream<CommandPart> parts = command.getParts().stream();
            if (iterator.hasNext()) {
                // drop the sub-command part
                parts = parts.filter(x -> !(x instanceof SubCommandPart));
            }
            PartHelper.appendUsage(parts, usage);
        }

        return usage.build();
    }

    public Component getFullHelp() {
        Command primary = Iterables.getLast(executionPath);
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
        Command primary = Iterables.getLast(executionPath);
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
        Command primary = Iterables.getLast(executionPath);
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
