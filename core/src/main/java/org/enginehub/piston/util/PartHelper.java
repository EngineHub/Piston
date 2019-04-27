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

package org.enginehub.piston.util;

import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import org.enginehub.piston.part.CommandPart;
import org.enginehub.piston.part.NoArgCommandFlag;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PartHelper {

    /**
     * Generate component representing the usage text for the given parts.
     */
    public static void appendUsage(Stream<CommandPart> parts, TextComponent.Builder output) {
        Stream.Builder<Component> other = Stream.builder();
        SortedSet<Character> flags = new TreeSet<>();
        Iterator<CommandPart> iterator = parts.iterator();
        while (iterator.hasNext()) {
            CommandPart part = iterator.next();
            if (part instanceof NoArgCommandFlag) {
                flags.add(((NoArgCommandFlag) part).getName());
            } else {
                other.add(part.getTextRepresentation());
            }
        }
        Stream<Component> flagsString = Stream.of(flags)
            .filter(x -> !x.isEmpty())
            .map(f -> f.stream()
                .map(String::valueOf)
                .collect(Collectors.joining("", "[-", "]")))
            .map(TextComponent::of);

        Iterator<Component> usages = Stream.concat(flagsString, other.build()).iterator();
        while (usages.hasNext()) {
            output.append(usages.next());
            if (usages.hasNext()) {
                output.append(TextComponent.of(" "));
            }
        }
    }

}
