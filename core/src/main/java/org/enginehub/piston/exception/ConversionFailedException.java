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

package org.enginehub.piston.exception;

import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import org.enginehub.piston.CommandParseResult;
import org.enginehub.piston.CommandValue;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.FailedConversion;
import org.enginehub.piston.converter.FailedConversionMapper;

import java.util.stream.Stream;

/**
 * Thrown from {@link CommandValue} when all conversions to a type have failed.
 */
public class ConversionFailedException extends UsageException {

    private static Component getMessage(Component conversionTarget, ArgumentConverter<?> converter,
                                        FailedConversion<?> conversion) {
        TextComponent.Builder builder = TextComponent.builder("")
            .append(TextComponent.of("Invalid value for "))
            .append(conversionTarget);
        if (conversion.getError().getMessage() != null) {
            builder.append(TextComponent.of(" (" + conversion.getError().getMessage() + ")"));
        }
        return builder
            .append(TextComponent.of(", acceptable values are "))
            .append(converter.describeAcceptableArguments())
            .build();
    }

    private final ArgumentConverter<?> converter;
    private final FailedConversion<?> conversion;

    public ConversionFailedException(CommandParseResult parseResult,
                                     Component conversionTarget,
                                     ArgumentConverter<?> converter,
                                     FailedConversion<?> conversion) {
        super(getMessage(conversionTarget, converter, conversion), parseResult);
        this.converter = converter;
        this.conversion = conversion;
        FailedConversionMapper.mapOnto(() -> this, conversion);
    }

    public FailedConversion<?> getConversion() {
        return conversion;
    }

    public Stream<Throwable> getAllErrors() {
        return Stream.concat(Stream.of(getCause()), Stream.of(getSuppressed()));
    }

    public ArgumentConverter<?> getConverter() {
        return converter;
    }
}
