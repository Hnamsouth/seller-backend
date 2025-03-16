package com.vtp.vipo.seller.common.utils;

import com.vtp.vipo.seller.common.constants.Constants;
import org.apache.commons.lang3.StringUtils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class StringProcessingUtils {

    private StringProcessingUtils() {
        throw new UnsupportedOperationException(Constants.UTITLITY_CLASS_ERROR);
    }

//    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
//            "(?:--|;|\\/\\*|\\*\\/|\b(SELECT|INSERT|UPDATE|DELETE|REPLACE|CREATE|ALTER|TRUNCATE|DROP|GRANT|REVOKE|COMMIT|ROLLBACK|SAVEPOINT|LOCK|UNLOCK|SET|CALL)\\b)\\s" +
//                    "|\\b(UNION(\\s+ALL)?\\s+SELECT|SELECT\\s+.*?\\s+FROM|AND\\s+.*?=|OR\\s+.*?=)" +
//                    "|\\b(EXEC|EXECUTE|TRIGGER|DECLARE|CURSOR|FETCH|BEGIN|END)\\b",
//            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL
//    );

    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(--|;|\\/\\*|\\*\\/|\\b(SELECT|INSERT|UPDATE|DELETE|REPLACE|DROP|UNION|EXEC|DECLARE|TRIGGER)\\b)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL
    );

    /**
     * Checks if the input string contains SQL injection threats.
     *
     * @param input the input string to validate
     * @return true if SQL injection threat is detected, false otherwise
     */
    public static boolean containsSqlInjectionThreats(String input) {
        return SQL_INJECTION_PATTERN.matcher(input).find();
    }

    public static boolean equals(final String s1, final String s2) {
        return s1 != null && s2 != null && s1.hashCode() == s2.hashCode()
                && s1.equals(s2);
    }

    public static String capitalizeEachWord(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }

        String[] words = str.split(" ");
        for (int i = 0; i < words.length; i++) {
            words[i] = StringUtils.capitalize(words[i]);
        }

        return String.join(" ", words);
    }

    public static String toSnakeCase(String s) {
        String[] parts = s.split("\\.");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
        }
        return String.join(".", parts);
    }

    /**
     * Safely converts an object to its string representation.
     * Returns null if the object is null or if its string representation is "null" (case-insensitive).
     *
     * @param obj the object to convert
     * @return the string representation of the object, or null if the object is null or "null"
     */
    public static String safeStringValue(Object obj) {
        // Return null if the object is null or its string representation is "null" (case-insensitive)
        if (obj == null || "null".equalsIgnoreCase(String.valueOf(obj))) {
            return null;
        }
        // Return the string representation of the object
        return String.valueOf(obj);
    }

    /**
     * Clean Vietnamese phoneNumber by removing the first "0" or the first "+84" or the first "84"
     */
    public static String cleanVietnamesePhoneNumber(String phoneNumber) {
        if (StringUtils.isBlank(phoneNumber)) {
            return phoneNumber;
        }

        // Remove specific country codes like '+84'
        if (phoneNumber.startsWith("+84")) {
            phoneNumber = phoneNumber.substring(3);
        } else if (phoneNumber.startsWith("84")) {
            phoneNumber = phoneNumber.substring(2);
        } else if (phoneNumber.startsWith("0")) {
            phoneNumber = phoneNumber.substring(1);
        }

        return phoneNumber;
    }

    /**
     * Strips leading and trailing whitespace from the input string and converts it to lowercase.
     * <p>
     * This method performs the following actions:
     * 1. Checks if the input string is blank or null. If it is, returns null.
     * 2. Strips any leading and trailing whitespace from the input string.
     * 3. Converts the stripped string to lowercase if it's not blank, otherwise returns null.
     * </p>
     *
     * @param input The string to be processed.
     * @return The processed string in lowercase without leading/trailing whitespace, or null if the input is blank or null.
     */
    public static String stripAndLowerCaseTheString(String input) {
        // Check if the input string is blank or null
        if (StringUtils.isBlank(input))
            return null;

        // Strip leading and trailing whitespace from the input
        String output = input.strip();

        // If the stripped string is not blank, convert it to lowercase and return
        return StringUtils.isNotBlank(output) ? output.toLowerCase() : null;
    }

    public static String normalizeVietnamese(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return Pattern
                .compile("\\p{InCombiningDiacriticalMarks}+")
                .matcher(normalized).replaceAll("")
                .replace("đ", "d").replace("Đ", "D");
    }

    public static String capitalizeUperCaseFirstLetterEachWord(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        String[] words = str.split(" ");
        StringBuilder capitalizedString = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                capitalizedString.append(word.substring(0, 1).toUpperCase()) // Capitalize first letter
                        .append(word.substring(1).toLowerCase()) // Keep the rest in lowercase
                        .append(" "); // Add space between words
            }
        }

        // Trim the last space
        return capitalizedString.toString().trim();
    }

    public static String trimLongString(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    public static String returnBlankWhenNull(String text) {
        return StringUtils.isBlank(text) ? "" : text;
    }

    public static String returnDefaultWhenNull(String text, String defaultValue) {
        return StringUtils.isBlank(text) ? defaultValue : text;
    }
}

