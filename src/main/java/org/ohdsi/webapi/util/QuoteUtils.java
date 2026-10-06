package org.ohdsi.webapi.util;

/**
 * Utility class for handling quoted strings.
 * Replaces com.odysseusinc.arachne.commons.utils.QuoteUtils
 */
public class QuoteUtils {

    /**
     * Removes surrounding quotes from a string if present.
     * Handles both single and double quotes.
     *
     * @param str the string to dequote
     * @return the string without surrounding quotes, or the original string if not quoted
     */
    public static String dequote(String str) {
        if (str == null || str.length() < 2) {
            return str;
        }

        char first = str.charAt(0);
        char last = str.charAt(str.length() - 1);

        if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
            return str.substring(1, str.length() - 1);
        }

        return str;
    }
}
