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

package org.enginehub.piston.converter;

import com.google.common.reflect.TypeToken;
import net.kyori.text.Component;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class ForwardingArgumentConverter<T> implements ArgumentConverter<T> {

    private final ArgumentConverter<T> delegate;

    protected ForwardingArgumentConverter(ArgumentConverter<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public ConversionResult<T> convert(Iterator<String> arguments, InjectedValueAccess context) {
        return delegate.convert(arguments, context);
    }

    @Override
    public Component describeAcceptableArguments() {
        return delegate.describeAcceptableArguments();
    }

    @Override
    public TypeToken<? extends T> getType() {
        return delegate.getType();
    }

    @Override
    public Stream<String> getSuggestions(String input) {
        return delegate.getSuggestions(input);
    }
}
