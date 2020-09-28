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
import net.kyori.adventure.text.TranslatableComponent;
import org.enginehub.piston.Command;
import org.enginehub.piston.config.ColorConfig;

import java.util.Collection;

import static org.enginehub.piston.util.ComponentHelper.joiningWithBar;

@AutoValue
public abstract class SubCommandPart implements ArgConsumingCommandPart {

    public static Builder builder(TranslatableComponent name, Component description) {
        return new AutoValue_SubCommandPart.Builder()
            .named(name)
            .describedBy(description);
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public final Builder named(TranslatableComponent name) {
            return argumentName(name);
        }

        abstract Builder argumentName(TranslatableComponent name);

        public final Builder describedBy(String description) {
            return describedBy(Component.text(description));
        }

        public final Builder describedBy(Component description) {
            return description(description);
        }

        abstract Builder description(Component description);

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

    public abstract ImmutableList<Command> getCommands();

    @Override
    public Component getTextRepresentation() {
        ImmutableList.Builder<Component> builder = ImmutableList.builder();
        builder.add(Component.text(isRequired() ? "<" : "["));
        builder.addAll(getCommands().stream()
            .map(Command::getName)
            .map(ColorConfig.mainText()::wrap)
            .collect(joiningWithBar())
            .children());
        builder.add(Component.text(isRequired() ? ">" : "]"));
        return ColorConfig.partWrapping().wrap(builder.build());
    }

}
