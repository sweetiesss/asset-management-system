package com.nashtech.rookies.oam.util;


import java.util.Arrays;
import java.util.stream.Collectors;

public final class StringUtil {
    private StringUtil() {
        // Prevent instantiation
    }

    /**
     * Extracts and concatenates the first character of each non-empty word
     * in the given string.
     *
     * <p>Words are assumed to be separated by spaces. Empty words (e.g.,
     * resulting from multiple spaces) are ignored.</p>
     *
     * @param str the input string from which to extract first characters
     * @return a string composed of the first characters of each non-empty word
     * in the input string
     * @throws NullPointerException if the input string is null
     */
    public static String getInitialsFromWords(final String str) {
        String[] words = str.split(" ");
        StringBuilder initials = new StringBuilder();

        Arrays.stream(words)
                .filter(word -> !word.isEmpty())
                .forEach(word -> initials.append(word.charAt(0)));

        return initials.toString();
    }

    /**
     * Capitalizes the first character of each word in the input string.
     * Words are considered to be space-separated. Extra spaces are trimmed.
     *
     * @param str the input string
     * @return the input string with each word's first character capitalized
     * @throws NullPointerException if the input string is null
     */
    public static String capitalizeWords(final String str) {
        return Arrays.stream(str.trim().split("\\s+"))
                .map(word -> word.isEmpty()
                        ? word
                        : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    /**
     * Normalizes a name string by trimming leading/trailing spaces and reducing
     * multiple internal spaces to a single space between words.
     *
     * <p>Example: "  John   Doe  " -> "John Doe"</p>
     *
     * @param str the input string
     * @return a normalized string with single spaces between words
     * @throws NullPointerException if the input string is null
     */
    public static String normalizeWhitespace(final String str) {
        return Arrays.stream(str.trim().split("\\s+"))
                .filter(word -> !word.isEmpty())
                .collect(Collectors.joining(" "));
    }

    /**
     * Removes all whitespace characters from the input string.
     *
     * <p>This includes spaces, tabs, newlines, and other Unicode-defined
     * whitespace characters.</p>
     *
     * @param str the input string
     * @return the string with all whitespace characters removed
     * @throws NullPointerException if the input string is null
     */
    public static String removeAllWhitespace(final String str) {
        return str.replaceAll("\\s+", "");
    }

}
