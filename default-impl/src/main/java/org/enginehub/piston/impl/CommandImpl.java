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

import org.enginehub.piston.Command;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.CommandFlag;
import org.enginehub.piston.part.CommandPart;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.enginehub.piston.Command.Action.NULL_ACTION;
import static com.google.common.base.Preconditions.checkState;

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
        Builder description(String description);

        @Override
        Builder footer(@Nullable String footer);

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
    public String getUsage() {
        StringBuilder builder = new StringBuilder();
        appendUsage(builder);
        return builder.toString();
    }

    private void appendUsage(StringBuilder builder) {
        builder.append('/').append(getName());
        Iterator<String> usages = PartHelper.getUsage(getParts()).iterator();
        while (usages.hasNext()) {
            builder.append(' ').append(usages.next());
        }
    }

    @Override
    public String getFullHelp() {
        StringBuilder builder = new StringBuilder(getDescription());

        builder.append("\nUsage: ");

        appendUsage(builder);
        builder.append('\n');

        appendArguments(builder);

        appendFlags(builder);

        getFooter().ifPresent(footer -> builder.append(footer).append('\n'));

        return builder.toString();
    }

    private void appendArguments(StringBuilder builder) {
        List<CommandArgument> args = getParts().stream()
            .filter(x -> x instanceof CommandArgument)
            .map(x -> (CommandArgument) x)
            .collect(Collectors.toList());
        if (args.size() > 0) {
            builder.append("Arguments:\n");
            for (CommandArgument arg : args) {
                builder.append("  ").append(arg.getTextRepresentation());
                if (arg.getDefaults().size() > 0) {
                    builder.append(" (defaults to ");
                    if (arg.getDefaults().size() == 1) {
                        builder.append(arg.getDefaults().get(0));
                    } else {
                        builder.append(arg.getDefaults());
                    }
                    builder.append(')');
                }
                builder.append(": ").append(arg.getDescription()).append('\n');
            }
        }
    }

    private void appendFlags(StringBuilder builder) {
        List<CommandFlag> flags = getParts().stream()
            .filter(x -> x instanceof CommandFlag)
            .map(x -> (CommandFlag) x)
            .collect(Collectors.toList());
        if (flags.size() > 0) {
            builder.append("Flags:\n");
            for (CommandFlag flag : flags) {
                // produces text like "-f: Some description"
                builder.append("  ").append(flag.getTextRepresentation())
                    .append(": ").append(flag.getDescription()).append('\n');
            }
        }
    }
}
