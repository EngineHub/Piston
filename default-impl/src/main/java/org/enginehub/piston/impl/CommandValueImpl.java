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

package org.enginehub.piston.impl;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import net.kyori.text.TextComponent;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandValue;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.exception.UsageException;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.part.CommandPart;

import java.util.Collection;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;

@AutoValue
abstract class CommandValueImpl implements CommandValue {

    static Builder builder() {
        return new AutoValue_CommandValueImpl.Builder();
    }

    @AutoValue.Builder
    interface Builder {

        Builder manager(CommandManager manager);

        Builder commandContext(Collection<Command> ctx);

        Builder partContext(CommandPart ctx);

        Builder injectedValues(InjectedValueAccess injectedValues);

        default Builder value(String value) {
            return values(ImmutableList.of(value));
        }

        Builder values(Collection<String> values);

        CommandValueImpl build();

    }

    CommandValueImpl() {
    }

    abstract CommandManager manager();

    abstract ImmutableList<Command> commandContext();

    abstract CommandPart partContext();

    abstract InjectedValueAccess injectedValues();

    abstract ImmutableList<String> values();

    @Override
    public ImmutableList<String> asStrings() {
        return values();
    }

    @Override
    public <T> ImmutableList<T> asMultiple(Key<T> key) {
        ImmutableList.Builder<T> values = ImmutableList.builder();
        for (String value : values()) {
            if (value.trim().isEmpty()) {
                continue;
            }
            Optional<ArgumentConverter<T>> converter = manager().getConverter(key);
            checkState(converter.isPresent(), "No converter for %s", key);
            ConversionResult<T> convert = converter.get().convert(value, injectedValues());
            if (!convert.isSuccessful()) {
                TextComponent.Builder message = TextComponent.builder("")
                    .append(TextComponent.of("Invalid value for "))
                    .append(partContext().getTextRepresentation())
                    .append(TextComponent.of(", acceptable values are "))
                    .append(converter.get().describeAcceptableArguments());
                throw new UsageException(message.build(), commandContext());
            }
            values.addAll(convert.get());
        }
        return values.build();
    }
}
