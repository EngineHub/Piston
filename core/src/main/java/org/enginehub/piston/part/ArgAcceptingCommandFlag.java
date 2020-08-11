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

package org.enginehub.piston.part;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import org.enginehub.piston.config.ColorConfig;
import org.enginehub.piston.inject.Key;

import java.util.Collection;

@AutoValue
public abstract class ArgAcceptingCommandFlag implements CommandFlag, ArgAcceptingCommandPart {

    public static Builder builder(char name, Component description) {
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
            return describedBy(TextComponent.of(description));
        }

        public final Builder describedBy(Component description) {
            return description(description);
        }

        abstract Builder description(Component description);

        public final Builder defaultsTo(Iterable<String> defaults) {
            return defaults(defaults);
        }

        abstract Builder defaults(Iterable<String> defaults);

        public final Builder ofTypes(Collection<Key<?>> types) {
            return types(types);
        }

        abstract Builder types(Collection<Key<?>> types);

        public final Builder argNamed(String name) {
            return argNamed(TranslatableComponent.of(name));
        }

        public final Builder argNamed(TranslatableComponent name) {
            return argumentName(name);
        }

        abstract Builder argumentName(TranslatableComponent name);

        public abstract ArgAcceptingCommandFlag build();
    }

    ArgAcceptingCommandFlag() {
    }

    @Override
    public Component getTextRepresentation() {
        return ColorConfig.partWrapping().wrap(
            TextComponent.of("["),
            ColorConfig.mainText().wrap("-" + getName()),
            TextComponent.space(),
            TextComponent.of("<"),
            ColorConfig.mainText().wrap(getArgumentName()),
            TextComponent.of(">"),
            TextComponent.of("]")
        );
    }
}
