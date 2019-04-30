package org.enginehub.piston.converter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SuggestionHelper {

    public static List<String> limitByPrefix(Stream<String> choices, String input) {
        return choices.filter(s -> startsWithIgnoreCase(s, input))
            .collect(Collectors.toList());
    }

    private static boolean startsWithIgnoreCase(String whole, String prefix) {
        return whole.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    private SuggestionHelper() {
    }
}
