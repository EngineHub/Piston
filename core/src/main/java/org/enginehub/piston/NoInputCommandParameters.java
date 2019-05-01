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

package org.enginehub.piston;

import com.google.auto.value.AutoValue;
import org.enginehub.piston.converter.ArgumentConverterAccess;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.part.ArgAcceptingCommandPart;
import org.enginehub.piston.part.CommandPart;

import javax.annotation.Nullable;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * An implementation of {@link CommandParameters} for situations where it is needed,
 * but there is no user input available.
 */
@AutoValue
public abstract class NoInputCommandParameters implements CommandParameters {

    public static Builder builder() {
        return new AutoValue_NoInputCommandParameters.Builder()
            .injectedValues(InjectedValueAccess.EMPTY)
            .converters(ArgumentConverterAccess.EMPTY);
    }

    @AutoValue.Builder
    public interface Builder {

        Builder injectedValues(InjectedValueAccess values);

        Builder metadata(@Nullable CommandMetadata metadata);

        Builder converters(ArgumentConverterAccess access);

        NoInputCommandParameters build();
    }

    NoInputCommandParameters() {
    }

    abstract InjectedValueAccess injectedValues();

    @Nullable
    abstract CommandMetadata metadata();

    abstract ArgumentConverterAccess converters();

    @Override
    public boolean has(CommandPart part) {
        return false;
    }

    @Override
    public CommandValue valueOf(ArgAcceptingCommandPart part) {
        throw new NoSuchElementException();
    }

    @Override
    @Nullable
    public CommandMetadata getMetadata() {
        return metadata();
    }

    @Override
    public ArgumentConverterAccess getConverters() {
        return converters();
    }

    @Override
    public <T> Optional<T> injectedValue(Key<T> key, InjectedValueAccess context) {
        return injectedValues().injectedValue(key, context);
    }

}
