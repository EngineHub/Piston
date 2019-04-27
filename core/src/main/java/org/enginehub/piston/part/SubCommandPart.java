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
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import net.kyori.text.TranslatableComponent;
import org.enginehub.piston.ColorConfig;
import org.enginehub.piston.Command;

import java.util.Collection;
import java.util.Iterator;

@AutoValue
public abstract class SubCommandPart implements CommandPart {

    public static Builder builder(TranslatableComponent name, Component description) {
        return new AutoValue_SubCommandPart.Builder()
            .named(name)
            .describedBy(description);
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public final Builder named(TranslatableComponent name) {
            return name(name);
        }

        abstract Builder name(TranslatableComponent name);

        public final Builder describedBy(String description) {
            return describedBy(TextComponent.of(description));
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

    public abstract TranslatableComponent getName();

    public abstract ImmutableList<Command> getCommands();

    @Override
    public Component getTextRepresentation() {
        TextComponent.Builder builder = TextComponent.builder("")
            .color(ColorConfig.getPartWrapping());
        builder.append(TextComponent.of(isRequired() ? "<" : "["));
        for (Iterator<Command> iterator = getCommands().iterator(); iterator.hasNext(); ) {
            Command command = iterator.next();

            builder.append(TextComponent.of(command.getName(), ColorConfig.getMainText()));
            if (iterator.hasNext()) {
                builder.append(TextComponent.of("|", ColorConfig.getTextModifier()));
            }
        }
        builder.append(TextComponent.of(isRequired() ? ">" : "]"));
        return builder.build();
    }

}
