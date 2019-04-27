package org.enginehub.piston.util;

import com.google.common.collect.ImmutableList;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import org.enginehub.piston.Command;
import org.enginehub.piston.part.CommandPart;
import org.enginehub.piston.part.SubCommandPart;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

import static net.kyori.text.Component.space;

public class UsageGenerator {

    public static UsageGenerator create(Collection<Command> executionPath) {
        return new UsageGenerator(ImmutableList.copyOf(executionPath));
    }

    private final ImmutableList<Command> executionPath;

    private UsageGenerator(ImmutableList<Command> executionPath) {
        this.executionPath = executionPath;
    }

    /**
     * Generate a name for the set of commands as a whole.
     */
    public Component getFullName() {
        TextComponent.Builder usage = TextComponent.builder("");

        for (Iterator<Command> iterator = executionPath.iterator();iterator.hasNext(); ) {
            Command command = iterator.next();
            usage.append(TextComponent.of(command.getName())).append(space());
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
            usage.append(TextComponent.of(command.getName())).append(space());
            Stream<CommandPart> parts = command.getParts().stream();
            if (iterator.hasNext()) {
                // drop the sub-command part
                parts = parts.filter(x -> !(x instanceof SubCommandPart));
            }
            PartHelper.appendUsage(parts, usage);
        }

        return usage.build();
    }

}
