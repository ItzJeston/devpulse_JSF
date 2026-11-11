package com.example.demo.util;

/**
 * Utility for HTML entity escaping and metadata formatting.
 * Implements manual sanitization to prevent Cross-Site Scripting (XSS)
 * without third-party libraries, matching Experiment 2a & 2b requirements.
 */
public final class SanitizationUtil {

    private SanitizationUtil() {
    }

    /**
     * Replaces HTML control characters with safe HTML entities.
     * Order of replacement is strictly: & -> &amp;, then <, >, ", '
     */
    public static String sanitizeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");
    }

    /**
     * Formats student full name and USN composite string.
     */
    public static String formatAuthorAndUsn(String name, String usn) {
        if (name == null && usn == null) {
            return "";
        }
        if (name == null) {
            return "(" + usn + ")";
        }
        if (usn == null) {
            return name;
        }
        return name + " (" + usn + ")";
    }

    /**
     * Normalizes email address to lower case.
     */
    public static String formatEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase();
    }
}
