package com.nashtech.rookies.oam.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StringUtilTest {

    private static Stream<Arguments> provideStringsForFirstChar() {
        return Stream.of(
                Arguments.of("John", "J"),
                Arguments.of("John Doe", "JD"),
                Arguments.of("John van Doe", "JvD"),
                Arguments.of("", ""),
                Arguments.of(" ", ""),
                Arguments.of("J", "J"),
                Arguments.of("J D", "JD"),
                Arguments.of("J D S", "JDS"),
                Arguments.of("John    Doe", "JD"),
                Arguments.of(" John  Doe ", "JD"),
                Arguments.of("  ", "")
        );
    }

    private static Stream<Arguments> provideStringsForCapitalizeWords() {
        return Stream.of(
                Arguments.of("john", "John"),
                Arguments.of("john doe", "John Doe"),
                Arguments.of("JOHN DOE", "John Doe"),
                Arguments.of("john DOE smith", "John Doe Smith"),
                Arguments.of("a", "A"),
                Arguments.of("", ""),
                Arguments.of(" ", ""),
                Arguments.of("  ", ""),
                Arguments.of("john    doe", "John Doe"),
                Arguments.of("  john  doe  ", "John Doe"),
                Arguments.of("john-doe", "John-doe"),
                Arguments.of("o'connor", "O'connor"),
                Arguments.of("123abc", "123abc"),
                Arguments.of("abc123", "Abc123"),
                Arguments.of("mIxEd CaSe WoRdS", "Mixed Case Words")
        );
    }

    private static Stream<Arguments> provideStringsForNormalizeWhitespace() {
        return Stream.of(
                Arguments.of("John Doe", "John Doe"),
                Arguments.of("  John   Doe  ", "John Doe"),
                Arguments.of("John    Doe    Smith", "John Doe Smith"),
                Arguments.of(" ", ""),
                Arguments.of("", ""),
                Arguments.of("  ", ""),
                Arguments.of("John", "John"),
                Arguments.of("  John  ", "John"),
                Arguments.of("John\tDoe", "John Doe"),
                Arguments.of("John\nDoe", "John Doe"),
                Arguments.of("John\r\nDoe", "John Doe"),
                Arguments.of("John\t\n  Doe   Smith", "John Doe Smith"),
                Arguments.of("a   b   c", "a b c")
        );
    }

    private static Stream<Arguments> provideStringsForRemoveAllWhitespace() {
        return Stream.of(
                Arguments.of("John Doe", "JohnDoe"),
                Arguments.of("  John   Doe  ", "JohnDoe"),
                Arguments.of("John    Doe    Smith", "JohnDoeSmith"),
                Arguments.of(" ", ""),
                Arguments.of("", ""),
                Arguments.of("  ", ""),
                Arguments.of("John", "John"),
                Arguments.of("  John  ", "John"),
                Arguments.of("John\tDoe", "JohnDoe"),
                Arguments.of("John\nDoe", "JohnDoe"),
                Arguments.of("John\r\nDoe", "JohnDoe"),
                Arguments.of("John\t\n  Doe   Smith", "JohnDoeSmith"),
                Arguments.of("a   b   c", "abc"),
                Arguments.of("Hello\u00A0World", "HelloWorld"), // Non-breaking space
                Arguments.of("Test\u2009String", "TestString") // Thin space
        );
    }

    // Tests for getInitialsFromWords method
    @Test
    void generateFirstCharOfStr_withSingleWord_shouldReturnFirstChar() {
        String input = "Smith";

        String result = StringUtil.getInitialsFromWords(input);

        assertEquals("S", result);
    }

    @Test
    void generateFirstCharOfStr_withMultipleWords_shouldReturnFirstCharOfEachWord() {
        String input = "John Doe Smith";

        String result = StringUtil.getInitialsFromWords(input);

        assertEquals("JDS", result);
    }

    @Test
    void getInitialsFromWords_withEmptyString_shouldReturnEmptyString() {
        String input = "";

        String result = StringUtil.getInitialsFromWords(input);

        assertEquals("", result);
    }

    @Test
    void getInitialsFromWords_withOnlySpaces_shouldReturnEmptyString() {
        String input = "   ";

        String result = StringUtil.getInitialsFromWords(input);

        assertEquals("", result);
    }

    @Test
    void getInitialsFromWords_withLeadingAndTrailingSpaces_shouldIgnoreExtraSpaces() {
        String input = "  John  Doe  ";

        String result = StringUtil.getInitialsFromWords(input);

        assertEquals("JD", result);
    }

    @Test
    void getInitialsFromWords_withMultipleConsecutiveSpaces_shouldHandleCorrectly() {
        String input = "John   Doe Smith";

        String result = StringUtil.getInitialsFromWords(input);

        assertEquals("JDS", result);
    }

    @Test
    void getInitialsFromWords_withSpecialCharacters_shouldWorkCorrectly() {
        String input = "John-Doe O'Smith";

        String result = StringUtil.getInitialsFromWords(input);

        assertEquals("JO", result);
    }

    @ParameterizedTest
    @MethodSource("provideStringsForFirstChar")
    void getInitialsFromWords_withVariousInputs_shouldReturnExpectedResults(
            String input, String expected) {
        assertEquals(expected, StringUtil.getInitialsFromWords(input));
    }

    @Test
    void getInitialsFromWords_nullInput_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.getInitialsFromWords(null));
    }

    // Tests for capitalizeWords method
    @Test
    void capitalizeWords_withSingleLowercaseWord_shouldCapitalizeFirstChar() {
        String input = "john";

        String result = StringUtil.capitalizeWords(input);

        assertEquals("John", result);
    }

    @Test
    void capitalizeWords_withSingleUppercaseWord_shouldCapitalizeFirstCharAndLowercaseRest() {
        String input = "JOHN";

        String result = StringUtil.capitalizeWords(input);

        assertEquals("John", result);
    }

    @Test
    void capitalizeWords_withMultipleWords_shouldCapitalizeEachWord() {
        String input = "john doe smith";

        String result = StringUtil.capitalizeWords(input);

        assertEquals("John Doe Smith", result);
    }

    @Test
    void capitalizeWords_withMixedCase_shouldNormalizeToProperCase() {
        String input = "jOhN dOe SmItH";

        String result = StringUtil.capitalizeWords(input);

        assertEquals("John Doe Smith", result);
    }

    @Test
    void capitalizeWords_withEmptyString_shouldReturnEmptyString() {
        String input = "";

        String result = StringUtil.capitalizeWords(input);

        assertEquals("", result);
    }

    @Test
    void capitalizeWords_withOnlySpaces_shouldReturnEmptyString() {
        String input = "   ";

        String result = StringUtil.capitalizeWords(input);

        assertEquals("", result);
    }

    @Test
    void capitalizeWords_withLeadingAndTrailingSpaces_shouldTrimAndCapitalize() {
        String input = "  john doe  ";

        String result = StringUtil.capitalizeWords(input);

        assertEquals("John Doe", result);
    }

    @Test
    void capitalizeWords_withMultipleConsecutiveSpaces_shouldNormalizeSpacesAndCapitalize() {
        String input = "john    doe   smith";

        String result = StringUtil.capitalizeWords(input);

        assertEquals("John Doe Smith", result);
    }

    @Test
    void capitalizeWords_withSingleCharacter_shouldCapitalize() {
        String input = "a";

        String result = StringUtil.capitalizeWords(input);

        assertEquals("A", result);
    }

    @Test
    void capitalizeWords_withSpecialCharacters_shouldCapitalizeWordsOnly() {
        String input = "john-doe o'connor";

        String result = StringUtil.capitalizeWords(input);

        assertEquals("John-doe O'connor", result);
    }

    @Test
    void capitalizeWords_withNumbers_shouldHandleCorrectly() {
        String input = "123 abc123 test";

        String result = StringUtil.capitalizeWords(input);

        assertEquals("123 Abc123 Test", result);
    }

    @Test
    void capitalizeWords_withTabsAndNewlines_shouldTreatAsSpaces() {
        String input = "john\tdoe\nsmith";

        String result = StringUtil.capitalizeWords(input);

        assertEquals("John Doe Smith", result);
    }

    @ParameterizedTest
    @MethodSource("provideStringsForCapitalizeWords")
    void capitalizeWords_withVariousInputs_shouldReturnExpectedResults(
            String input, String expected) {
        assertEquals(expected, StringUtil.capitalizeWords(input));
    }

    @Test
    void capitalizeWords_nullInput_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.capitalizeWords(null));
    }

    // Tests for normalizeWhitespace method
    @Test
    void normalizeWhitespace_withNormalSpacing_shouldReturnUnchanged() {
        String input = "John Doe";

        String result = StringUtil.normalizeWhitespace(input);

        assertEquals("John Doe", result);
    }

    @Test
    void normalizeWhitespace_withLeadingAndTrailingSpaces_shouldTrim() {
        String input = "  John Doe  ";

        String result = StringUtil.normalizeWhitespace(input);

        assertEquals("John Doe", result);
    }

    @Test
    void normalizeWhitespace_withMultipleSpaces_shouldNormalizeToSingleSpaces() {
        String input = "John    Doe    Smith";

        String result = StringUtil.normalizeWhitespace(input);

        assertEquals("John Doe Smith", result);
    }

    @Test
    void normalizeWhitespace_withOnlySpaces_shouldReturnEmptyString() {
        String input = "   ";

        String result = StringUtil.normalizeWhitespace(input);

        assertEquals("", result);
    }

    @Test
    void normalizeWhitespace_withEmptyString_shouldReturnEmptyString() {
        String input = "";

        String result = StringUtil.normalizeWhitespace(input);

        assertEquals("", result);
    }

    @Test
    void normalizeWhitespace_withSingleWord_shouldReturnWord() {
        String input = "John";

        String result = StringUtil.normalizeWhitespace(input);

        assertEquals("John", result);
    }

    @Test
    void normalizeWhitespace_withSingleWordAndSpaces_shouldTrimSpaces() {
        String input = "  John  ";

        String result = StringUtil.normalizeWhitespace(input);

        assertEquals("John", result);
    }

    @Test
    void normalizeWhitespace_withTabsAndNewlines_shouldNormalizeToSpaces() {
        String input = "John\tDoe\nSmith";

        String result = StringUtil.normalizeWhitespace(input);

        assertEquals("John Doe Smith", result);
    }

    @Test
    void normalizeWhitespace_withMixedWhitespace_shouldNormalizeAll() {
        String input = "John\t\n  Doe   Smith";

        String result = StringUtil.normalizeWhitespace(input);

        assertEquals("John Doe Smith", result);
    }

    @ParameterizedTest
    @MethodSource("provideStringsForNormalizeWhitespace")
    void normalizeWhitespace_withVariousInputs_shouldReturnExpectedResults(
            String input, String expected) {
        assertEquals(expected, StringUtil.normalizeWhitespace(input));
    }

    @Test
    void normalizeWhitespace_nullInput_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.normalizeWhitespace(null));
    }

    // Tests for removeAllWhitespace method
    @Test
    void removeAllWhitespace_withNormalSpaces_shouldRemoveSpaces() {
        String input = "John Doe";

        String result = StringUtil.removeAllWhitespace(input);

        assertEquals("JohnDoe", result);
    }

    @Test
    void removeAllWhitespace_withMultipleSpaces_shouldRemoveAllSpaces() {
        String input = "John    Doe    Smith";

        String result = StringUtil.removeAllWhitespace(input);

        assertEquals("JohnDoeSmith", result);
    }

    @Test
    void removeAllWhitespace_withLeadingAndTrailingSpaces_shouldRemoveAll() {
        String input = "  John Doe  ";

        String result = StringUtil.removeAllWhitespace(input);

        assertEquals("JohnDoe", result);
    }

    @Test
    void removeAllWhitespace_withOnlySpaces_shouldReturnEmptyString() {
        String input = "   ";

        String result = StringUtil.removeAllWhitespace(input);

        assertEquals("", result);
    }

    @Test
    void removeAllWhitespace_withEmptyString_shouldReturnEmptyString() {
        String input = "";

        String result = StringUtil.removeAllWhitespace(input);

        assertEquals("", result);
    }

    @Test
    void removeAllWhitespace_withNoWhitespace_shouldReturnUnchanged() {
        String input = "JohnDoe";

        String result = StringUtil.removeAllWhitespace(input);

        assertEquals("JohnDoe", result);
    }

    @Test
    void removeAllWhitespace_withTabsAndNewlines_shouldRemoveAll() {
        String input = "John\tDoe\nSmith";

        String result = StringUtil.removeAllWhitespace(input);

        assertEquals("JohnDoeSmith", result);
    }

    @Test
    void removeAllWhitespace_withCarriageReturnAndNewline_shouldRemoveAll() {
        String input = "John\r\nDoe";

        String result = StringUtil.removeAllWhitespace(input);

        assertEquals("JohnDoe", result);
    }

    @Test
    void removeAllWhitespace_withMixedWhitespace_shouldRemoveAll() {
        String input = "John\t\n  Doe   Smith";

        String result = StringUtil.removeAllWhitespace(input);

        assertEquals("JohnDoeSmith", result);
    }

    @Test
    void removeAllWhitespace_nullInput_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.removeAllWhitespace(null));
    }
}