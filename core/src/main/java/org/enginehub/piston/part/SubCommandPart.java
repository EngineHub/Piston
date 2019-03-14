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

package org.enginehub.piston.part;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.enginehub.piston.Command;

import java.util.Collection;

import static java.util.stream.Collectors.joining;

@AutoValue
public abstract class SubCommandPart implements CommandPart {

    public static Builder builder(String name, String description) {
        return new AutoValue_SubCommandPart.Builder()
            .named(name)
            .describedBy(description);
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public final Builder named(String name) {
            return name(name);
        }

        abstract Builder name(String name);

        public final Builder describedBy(String description) {
            return description(description);
        }

        abstract Builder description(String description);

        public final Builder withCommands(Collection<Command> commands) {
            return commands(commands);
        }

        abstract Builder commands(Collection<Command> commands);

        public final Builder required() {
            return required(true);
        }

        public final Builder optional() {
            return required(false);
        }

        abstract Builder required(boolean required);

        public abstract SubCommandPart build();
    }

    public abstract String getName();

    public abstract ImmutableList<Command> getCommands();

    @Override
    public String getTextRepresentation() {
        String cmdsJoined = getCommands().stream()
            .map(Command::getName)
            .collect(joining("|"));
        return isRequired() ? "<" + cmdsJoined + ">" : "[" + cmdsJoined + "]";
    }

}
