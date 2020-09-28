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
import net.kyori.adventure.text.Component;
import org.enginehub.piston.config.ColorConfig;

@AutoValue
public abstract class NoArgCommandFlag implements CommandFlag {

    public static NoArgCommandFlag.Builder builder(char name,
                                                   String description) {
        return builder(name, Component.text(description));
    }

    public static NoArgCommandFlag.Builder builder(char name,
                                                   Component description) {
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
            return describedBy(Component.text(description));
        }

        public final Builder describedBy(Component description) {
            return description(description);
        }

        abstract Builder description(Component description);

        public final ArgAcceptingCommandFlag.Builder withRequiredArg() {
            NoArgCommandFlag flag = build();
            return ArgAcceptingCommandFlag.builder(
                flag.getName(),
                flag.getDescription()
            );
        }

        public abstract NoArgCommandFlag build();

    }

    NoArgCommandFlag() {
    }

    @Override
    public Component getTextRepresentation() {
        return ColorConfig.partWrapping().wrap(
            Component.text("["),
            ColorConfig.mainText().wrap("-" + getName()),
            Component.text("]")
        );
    }
}
