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

package org.enginehub.piston.suggestion

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.enginehub.piston.converter.ArgumentConverter
import org.enginehub.piston.converter.ConversionResult
import org.enginehub.piston.converter.FailedConversion
import org.enginehub.piston.converter.SuccessfulConversion
import org.enginehub.piston.converter.SuggestionHelper.limitByPrefix
import org.enginehub.piston.inject.InjectedValueAccess

class SimpleSuggestingConverter(private val suggestions: List<String>) : ArgumentConverter<String> {
    override fun convert(argument: String, context: InjectedValueAccess): ConversionResult<String> {
        return when (argument) {
            in suggestions -> SuccessfulConversion.fromSingle(argument)
            else -> FailedConversion.from(IllegalArgumentException("Not a valid argument: $argument"))
        }
    }

    override fun describeAcceptableArguments(): Component {
        return Component.text("Any of $suggestions")
    }

    override fun getSuggestions(input: String, context: InjectedValueAccess): List<String> =
        limitByPrefix(suggestions.stream(), input)

}
