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
import com.google.common.collect.ImmutableSet;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import net.kyori.text.TranslatableComponent;
import org.enginehub.piston.config.ColorConfig;
import org.enginehub.piston.inject.Key;

import java.util.Collection;

@AutoValue
public abstract class CommandArgument implements ArgAcceptingCommandPart {

    public static Builder builder(TranslatableComponent name, Component description) {
        return new AutoValue_CommandArgument.Builder()
            .named(name)
            .describedBy(description)
            .defaultsTo(ImmutableList.of())
            .ofTypes(ImmutableSet.of())
            .variable(false);
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public final Builder named(String name) {
            return named(TranslatableComponent.of(name));
        }

        public final Builder named(TranslatableComponent name) {
            return argumentName(name);
        }

        abstract Builder argumentName(TranslatableComponent name);

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

        public abstract Builder variable(boolean variable);

        public abstract CommandArgument build();
    }

    /**
     * Check if this argument a <em>variable argument</em>.
     *
     * That is, does it accept a variable amount of inputs, rather than one?
     */
    public abstract boolean isVariable();

    /**
     * {@inheritDoc}
     *
     * <p>
     * Arguments are always required when they have no defaults.
     * To provide a {@code null} default, use the empty string.
     * </p>
     */
    @Override
    public final boolean isRequired() {
        return getDefaults().isEmpty();
    }

    @Override
    public Component getTextRepresentation() {
        ImmutableList.Builder<Component> builder = ImmutableList.builder();
        builder.add(TextComponent.of(isRequired() ? "<" : "["));
        builder.add(ColorConfig.mainText().wrap(getArgumentName()));
        if (isVariable()) {
            builder.add(ColorConfig.textModifier().wrap("..."));
        }
        builder.add(TextComponent.of(isRequired() ? ">" : "]"));
        return ColorConfig.partWrapping().wrap(builder.build());
    }

}
