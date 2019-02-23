package com.enginehub.piston.util;

import com.google.common.base.Splitter;

import java.util.stream.IntStream;

import static com.google.common.collect.Streams.concat;
import static com.google.common.collect.Streams.stream;
import static java.util.stream.Collectors.joining;

public class CaseHelper {

    // Splits on capital letters, without consuming the letter.
    // Regex explained:
    // (?<!^|\\s) -- negative lookbehind for start of line, prevents split if capital is the first
    // letter or start of a word
    // (?=\p{Lu}) -- positive lookahead for uppercase letters, according to Unicode.
    private static final Splitter CAPITAL_SPLITTER = Splitter.onPattern("(?<!^|\\s)(?=\\p{Lu})")
            .omitEmptyStrings();

    /**
     * Convert a string from title case to spaced lower case.
     *
     * <p>
     * Example: {@code "SomeClassName"} -> {@code "some class name"}.
     * </p>
     *
     * @param titleCase the string that is in title case
     * @return a string in spaced lower case
     */
    public static String titleToSpacedLower(String titleCase) {
        return stream(CAPITAL_SPLITTER.split(titleCase))
                .map(x -> {
                    int firstCp = Character.toLowerCase(x.codePointAt(0));
                    IntStream restCp = x.codePoints().skip(1);
                    return concat(IntStream.of(firstCp), restCp)
                            .collect(() -> new StringBuilder(x.length()),
                                    StringBuilder::appendCodePoint,
                                    StringBuilder::append);
                }).collect(joining(" "));
    }

}
