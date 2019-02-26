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

import com.enginehub.piston.converter.ArgumentConverter;
import com.enginehub.piston.converter.ArgumentConverters;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;

@AutoValue
public abstract class ArgAcceptingCommandFlag<T> implements CommandFlag, ArgAcceptingCommandPart<T> {

    public static <T> Builder<T> builder(char name,
                                         String description,
                                         ArgumentConverter<T> converter) {
        return new AutoValue_ArgAcceptingCommandFlag.Builder<>()
            .named(name)
            .describedBy(description)
            .convertedBy(converter)
            .defaultsTo(ImmutableList.of());
    }

    @AutoValue.Builder
    public abstract static class Builder<T> {

        public final Builder<T> named(char name) {
            return name(name);
        }

        abstract Builder<T> name(char name);

        public final Builder<T> describedBy(String description) {
            return description(description);
        }

        abstract Builder<T> description(String description);

        public final <U> Builder<U> ofType(Class<U> type) {
            return ofType(TypeToken.of(type));
        }

        public final <U> Builder<U> ofType(TypeToken<U> type) {
            return convertedBy(ArgumentConverters.get(type));
        }

        // auto-value workaround, since it can't tell we're changing the type of the whole
        // builder!
        @SuppressWarnings("unchecked")
        public final <U> Builder<U> convertedBy(ArgumentConverter<U> converter) {
            return (Builder<U>) converter((ArgumentConverter<T>) converter);
        }

        abstract Builder<T> converter(ArgumentConverter<T> converter);

        public final Builder<T> defaultsTo(Iterable<T> defaults) {
            return defaults(defaults);
        }

        abstract Builder<T> defaults(Iterable<T> defaults);

        public abstract ArgAcceptingCommandFlag<T> build();
    }

    ArgAcceptingCommandFlag() {
    }

    @Override
    public String getTextRepresentation() {
        return "[-" + getName() +
            " <" + getConverter().describeAcceptableArguments() + ">]";
    }
}
