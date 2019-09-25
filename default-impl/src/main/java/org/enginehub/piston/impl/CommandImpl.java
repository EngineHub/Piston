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

package org.enginehub.piston.impl;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import org.enginehub.piston.Command;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.CommandFlag;
import org.enginehub.piston.part.CommandPart;
import org.enginehub.piston.suggestion.DefaultSuggestionProvider;
import org.enginehub.piston.suggestion.SuggestionProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static net.kyori.text.TextComponent.newline;
import static org.enginehub.piston.Command.Action.NULL_ACTION;

@AutoValue
abstract class CommandImpl implements Command {

    static Builder builder(String name) {
        return new AutoValue_CommandImpl.Builder()
            .footer(null)
            .condition(Condition.TRUE)
            .name(name)
            .aliases(ImmutableList.of())
            .parts(ImmutableList.of())
            .action(NULL_ACTION)
            .suggester(DefaultSuggestionProvider.getInstance());
    }

    @AutoValue.Builder
    interface Builder extends Command.Builder {

        @Override
        Builder name(String name);

        @Override
        Builder aliases(Collection<String> aliases);

        @Override
        Builder description(Component description);

        @Override
        Builder footer(@Nullable Component footer);

        @Override
        Builder parts(Collection<CommandPart> parts);

        @Override
        Builder action(Action action);

        @Override
        Builder condition(Condition condition);

        @Override
        Builder suggester(SuggestionProvider suggester);

        ImmutableList.Builder<CommandPart> partsBuilder();

        @Override
        default Builder addPart(CommandPart part) {
            partsBuilder().add(part);
            return this;
        }

        @Override
        default Builder addParts(CommandPart... parts) {
            partsBuilder().add(parts);
            return this;
        }

        @Override
        default Builder addParts(Iterable<CommandPart> parts) {
            partsBuilder().addAll(parts);
            return this;
        }

        CommandImpl autoBuild();

        @Override
        default CommandImpl build() {
            CommandImpl auto = autoBuild();
            checkState(auto.getName().length() > 0, "command name must not be empty");
            return auto;
        }

    }

    @Override
    public abstract Builder toBuilder();

}
