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
