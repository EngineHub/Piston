package com.enginehub.piston.impl;

import com.enginehub.piston.Command;
import com.enginehub.piston.part.CommandArgument;
import com.enginehub.piston.part.CommandFlag;
import com.enginehub.piston.part.CommandPart;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.enginehub.piston.Command.Action.NULL_ACTION;
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
        StringBuilder builder = new StringBuilder("Usage: ");

        appendUsage(builder);
        builder.append('\n');

        appendArguments(builder);

        appendFlags(builder);

        getFooter().ifPresent(footer -> builder.append(footer).append('\n'));

        return builder.toString();
    }

    private void appendArguments(StringBuilder builder) {
        List<CommandArgument<?>> args = getParts().stream()
                .filter(x -> x instanceof CommandArgument)
                .map(x -> (CommandArgument<?>) x)
                .collect(Collectors.toList());
        if (args.size() > 0) {
            builder.append("Arguments:\n");
            for (CommandArgument<?> arg : args) {
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
