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
import com.google.inject.Key;

import java.util.Collection;

@AutoValue
public abstract class ArgAcceptingCommandFlag implements CommandFlag, ArgAcceptingCommandPart {

    public static Builder builder(char name, String description) {
        return new AutoValue_ArgAcceptingCommandFlag.Builder()
            .named(name)
            .describedBy(description)
            .defaultsTo(ImmutableList.of())
            .ofTypes(ImmutableList.of());
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public final Builder named(char name) {
            return name(name);
        }

        abstract Builder name(char name);

        public final Builder describedBy(String description) {
            return description(description);
        }

        abstract Builder description(String description);

        public final Builder defaultsTo(Iterable<String> defaults) {
            return defaults(defaults);
        }

        abstract Builder defaults(Iterable<String> defaults);

        public final Builder ofTypes(Collection<Key<?>> types) {
            return types(types);
        }

        abstract Builder types(Collection<Key<?>> types);

        public final Builder argNamed(String name) {
            return argumentName(name);
        }

        abstract Builder argumentName(String name);

        public abstract ArgAcceptingCommandFlag build();
    }

    ArgAcceptingCommandFlag() {
    }

    public abstract String getArgumentName();

    @Override
    public String getTextRepresentation() {
        return "[-" + getName() + " <" + getArgumentName() + ">]";
    }
}
