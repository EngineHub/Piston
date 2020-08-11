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

package org.enginehub.piston.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.enginehub.piston.Command;
import org.enginehub.piston.config.ColorConfig;
import org.enginehub.piston.part.CommandPart;
import org.enginehub.piston.part.NoArgCommandFlag;
import org.enginehub.piston.part.SubCommandPart;

import java.util.Iterator;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;

public class PartHelper {

    /**
     * Generate component representing the usage text for the given parts.
     */
    public static void appendUsage(Stream<CommandPart> parts, TextComponent.Builder output) {
        Stream.Builder<Component> other = Stream.builder();
        SortedSet<Character> flags = new TreeSet<>();
        Iterator<CommandPart> iterator = parts.iterator();
        Stream.Builder<Component> postOSC = Stream.builder();
        SubCommandPart optionalSubCommand = null;
        while (iterator.hasNext()) {
            CommandPart part = iterator.next();
            if (part instanceof NoArgCommandFlag) {
                // This is not a necessary restriction, but it simplified logic here
                // If you need it, make an issue, and this can be rewritten to support it.
                checkState(optionalSubCommand == null,
                    "All flags should come before sub-commands.");
                flags.add(((NoArgCommandFlag) part).getName());
                continue;
            }

            if (part instanceof SubCommandPart) {
                // Make an optional sub-command part bind with the rest of the parts.
                if (!part.isRequired()) {
                    optionalSubCommand = (SubCommandPart) part;
                    continue;
                }
            }

            (optionalSubCommand == null ? other : postOSC).add(part.getTextRepresentation());
        }
        Stream<Component> flagsString = Optional.of(flags)
            .filter(x -> !x.isEmpty())
            .map(f -> f.stream()
                .map(String::valueOf)
                .collect(Collectors.joining("")))
            .map(text -> ColorConfig.partWrapping().wrap(
                TextComponent.of("["),
                ColorConfig.mainText().wrap("-" + text),
                TextComponent.of("]")
            ))
            .map(Stream::of).orElse(Stream.empty());

        Stream<Component> afterFlags = optionalSubCommand == null
            ? other.build()
            : Stream.concat(other.build(), buildOptionalMerging(optionalSubCommand, postOSC.build()));

        Iterator<Component> usages = Stream.concat(flagsString, afterFlags).iterator();
        while (usages.hasNext()) {
            output.append(usages.next());
            if (usages.hasNext()) {
                output.append(TextComponent.of(" "));
            }
        }
    }

    private static Stream<Component> buildOptionalMerging(SubCommandPart optionalSubCommand,
                                                          Stream<Component> postComponents) {
        return Stream.of(ColorConfig.partWrapping().wrap(
            TextComponent.of("<"),
            optionalSubCommand.getCommands().stream()
                .map(Command::getName)
                .map(ColorConfig.mainText()::wrap)
                .collect(ComponentHelper.joiningWithBar()),
            TextComponent.of("|"),
            postComponents.collect(ComponentHelper.joiningTexts(
                TextComponent.empty(),
                TextComponent.of(" "),
                TextComponent.empty()
            )),
            TextComponent.of(">")
        ));
    }

}
