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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.enginehub.piston.CommandMetadata;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.CommandValue;
import org.enginehub.piston.converter.ArgumentConverterAccess;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.part.ArgAcceptingCommandPart;
import org.enginehub.piston.part.CommandPart;

import java.util.NoSuchElementException;
import java.util.Optional;

@AutoValue
abstract class CommandParametersImpl implements CommandParameters {

    static Builder builder() {
        return new AutoValue_CommandParametersImpl.Builder();
    }

    @AutoValue.Builder
    interface Builder {

        default Builder addPresentPart(CommandPart part) {
            presentPartsBuilder().add(part);
            return this;
        }

        ImmutableSet.Builder<CommandPart> presentPartsBuilder();

        default Builder addValue(CommandPart part, CommandValue value) {
            valuesBuilder().put(part, value);
            return this;
        }

        ImmutableMap.Builder<CommandPart, CommandValue> valuesBuilder();

        Builder injectedValues(InjectedValueAccess values);

        Builder metadata(CommandMetadata metadata);

        Builder converters(ArgumentConverterAccess converters);

        CommandParametersImpl build();
    }

    CommandParametersImpl() {
    }

    abstract ImmutableSet<CommandPart> presentParts();

    abstract ImmutableMap<CommandPart, CommandValue> values();

    abstract InjectedValueAccess injectedValues();

    abstract CommandMetadata metadata();

    abstract ArgumentConverterAccess converters();

    @Override
    public final boolean has(CommandPart part) {
        return presentParts().contains(part);
    }

    @Override
    public final CommandValue valueOf(ArgAcceptingCommandPart part) {
        CommandValue value = values().get(part);
        if (value == null) {
            throw new NoSuchElementException("No value for " + part);
        }
        return value;
    }

    @Override
    public CommandMetadata getMetadata() {
        return metadata();
    }

    @Override
    public ArgumentConverterAccess getConverters() {
        return converters();
    }

    @Override
    public final <T> Optional<T> injectedValue(Key<T> key, InjectedValueAccess context) {
        return injectedValues().injectedValue(key, context);
    }
}
