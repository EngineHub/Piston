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

package org.enginehub.piston.impl;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import org.enginehub.piston.Command;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.CommandFlag;
import org.enginehub.piston.part.CommandPart;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static net.kyori.text.Component.newline;
import static org.enginehub.piston.Command.Action.NULL_ACTION;

@AutoValue
abstract class CommandImpl implements Command {

    static Builder builder(String name) {
        return new AutoValue_CommandImpl.Builder()
            .footer(null)
            .condition(Condition.TRUE)
            .name(name)
            .parts(ImmutableList.of())
            .action(NULL_ACTION);
    }

    @AutoValue.Builder
    interface Builder extends Command.Builder {

        @Override
        Builder name(String name);

        @Override
        Builder aliases(Collection<String> aliases);

        @Override
        Builder description(Component description);

        @Override
        Builder footer(@Nullable Component footer);

        @Override
        Builder parts(Collection<CommandPart> parts);

        @Override
        Builder action(Action action);

        @Override
        Builder condition(Condition condition);

        ImmutableList.Builder<CommandPart> partsBuilder();

        @Override
        default Builder addPart(CommandPart part) {
            partsBuilder().add(part);
            return this;
        }

        @Override
        default Builder addParts(CommandPart... parts) {
            partsBuilder().add(parts);
            return this;
        }

        @Override
        default Builder addParts(Iterable<CommandPart> parts) {
            partsBuilder().addAll(parts);
            return this;
        }

        CommandImpl autoBuild();

        @Override
        default CommandImpl build() {
            CommandImpl auto = autoBuild();
            checkState(auto.getName().length() > 0, "command name must not be empty");
            return auto;
        }

    }

    @Override
    public abstract Builder toBuilder();

    @Override
    public Component getFullHelp() {
        TextComponent.Builder builder = TextComponent.builder("");

        builder.append(getDescription());

        builder.append(TextComponent.of("\nUsage: "));

        builder.append(getUsage());
        builder.append(newline());

        appendArguments(builder);

        appendFlags(builder);

        getFooter().ifPresent(footer -> builder.append(footer).append(newline()));

        return builder.build();
    }

    private void appendArguments(TextComponent.Builder builder) {
        List<CommandArgument> args = getParts().stream()
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
        List<CommandFlag> flags = getParts().stream()
            .filter(x -> x instanceof CommandFlag)
            .map(x -> (CommandFlag) x)
            .collect(Collectors.toList());
        if (flags.size() > 0) {
            builder.append(TextComponent.of("Flags:\n"));
            for (CommandFlag flag : flags) {
                // produces text like "-f: Some description"
                builder.append(TextComponent.of("  -"))
                    .append(Component.of(flag.getName()))
                    .append(TextComponent.of(": "))
                    .append(flag.getDescription())
                    .append(newline());
            }
        }
    }
}
