/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) EngineHub <http://www.enginehub.com>
 * Copyright (C) oblique-commands contributors
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

package com.enginehub.piston.impl;

import com.enginehub.piston.part.CommandPart;
import com.enginehub.piston.part.NoArgCommandFlag;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PartHelper {

    public static Stream<String> getUsage(List<CommandPart> parts) {
        Stream.Builder<String> other = Stream.builder();
        SortedSet<Character> flags = new TreeSet<>();
        for (CommandPart part : parts) {
            if (part instanceof NoArgCommandFlag) {
                flags.add(((NoArgCommandFlag) part).getName());
            } else {
                other.add(part.getTextRepresentation());
            }
        }
        Stream<String> flagsString = Stream.of(flags)
            .filter(x -> !x.isEmpty())
            .map(f -> f.stream()
                .map(String::valueOf)
                .collect(Collectors.joining("", "[-", "]")));
        return Stream.concat(flagsString, other.build());
    }

}
