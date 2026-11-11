package com.example.demo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Procedural validation utility implementing manual regular expression
 * matching and boundary validations matching Experiment 2a & 2b requirements.
 */
public final class ValidationUtil {

    private static final Pattern USN_PATTERN = Pattern.compile("^NNM\\d{2}CS\\d{3}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s.]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$");

    private ValidationUtil() {
    }

    public static boolean isValidUsn(String usn) {
        if (usn == null) {
            return false;
        }
        return USN_PATTERN.matcher(usn).matches();
    }

    public static boolean isValidStudentName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return NAME_PATTERN.matcher(name).matches();
    }

    public static boolean isValidLabExpNo(int expNo) {
        return expNo >= 1 && expNo <= 10;
    }

    public static boolean isValidLabExpNo(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        try {
            int exp = Integer.parseInt(input.trim());
            return isValidLabExpNo(exp);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidLanguage(String language) {
        if (language == null) {
            return false;
        }
        return "Java".equals(language) || "JSP".equals(language) || "SQL".equals(language) || "HTML".equals(language);
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isNonEmpty(String input) {
        return input != null && !input.trim().isEmpty();
    }

    public static List<String> validateSnippetSubmission(
            String usn,
            String studentName,
            String email,
            String labExpNo,
            String language,
            String title,
            String rawCode) {

        List<String> errors = new ArrayList<>();

        if (!isValidUsn(usn)) {
            errors.add("USN must follow valid format: ^NNM\\d{2}CS\\d{3}$");
        }
        if (!isValidStudentName(studentName)) {
            errors.add("Name must contain only letters and spaces (dots allowed)");
        }
        if (!isValidLabExpNo(labExpNo)) {
            errors.add("Lab Experiment No. must be a valid numeric value between 1 and 10");
        }
        if (!isValidLanguage(language)) {
            errors.add("Invalid language selected (must be Java, JSP, SQL, or HTML)");
        }
        if (!isValidEmail(email)) {
            errors.add("Email must be in a valid format");
        }
        if (!isNonEmpty(title)) {
            errors.add("Title cannot be empty");
        }
        if (!isNonEmpty(rawCode)) {
            errors.add("Code snippet cannot be empty");
        }

        return errors;
    }
}
