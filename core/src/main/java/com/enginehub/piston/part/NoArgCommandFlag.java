/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) EngineHub <http://www.enginehub.com>
 * Copyright (C) oblique-commands contributors
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

package com.enginehub.piston.part;

import com.enginehub.piston.converter.ArgumentConverters;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class NoArgCommandFlag implements CommandFlag {

    public static NoArgCommandFlag.Builder builder(char name,
                                                   String description) {
        return new AutoValue_NoArgCommandFlag.Builder()
            .named(name)
            .describedBy(description);
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

        public final ArgAcceptingCommandFlag.Builder<String> withRequiredArg() {
            NoArgCommandFlag flag = build();
            return ArgAcceptingCommandFlag.builder(
                flag.getName(),
                flag.getDescription(),
                ArgumentConverters.forString()
            );
        }

        public abstract NoArgCommandFlag build();

    }

    NoArgCommandFlag() {
    }

    @Override
    public String getTextRepresentation() {
        return "[-" + getName() + "]";
    }
}
