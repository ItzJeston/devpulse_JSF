package com.example.demo.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure JUnit 5 Jupiter unit test suite for procedural validation and XSS sanitization.
 * Exercises Experiments 2a and 2b: regex validation, boundary checks, entity escaping,
 * string formatting, and comprehensive error aggregation without Spring/DB overhead.
 */
public class ValidationAndSanitizationTest {

    // =========================================================================
    // 1. USN VALIDATION TESTS: ^NNM\d{2}CS\d{3}$ (Feature 15)
    // =========================================================================
    @Nested
    @DisplayName("USN Validation Regex (^NNM\\d{2}CS\\d{3}$)")
    class UsnValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "NNM24CS107", // Jeston Correa
            "NNM24CS108", // Jnanesh Karkera
            "NNM24CS100", // Ian Roniel Soans
            "NNM24CS076", // Dhanush U. Shetty
            "NNM22CS001", // Prior batch
            "NNM99CS999"  // High boundary
        })
        @DisplayName("F15 [Tier 1]: Valid USN patterns are accepted")
        void testValidUsnPatterns(String usn) {
            assertTrue(ValidationUtil.isValidUsn(usn), "USN should be valid: " + usn);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "nnm24cs107",
            "NNM24cs107",
            "nnm24CS107"
        })
        @DisplayName("F15 [Tier 2]: Lowercase and mixed-case prefixes/branches rejected")
        void testInvalidUsn_CaseSensitivity(String usn) {
            assertFalse(ValidationUtil.isValidUsn(usn), "Lowercase USN must be rejected: " + usn);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "NNM24IS107", // Information Science
            "NNM24EC107", // Electronics
            "NNM24ME107", // Mechanical
            "NNM24CV107", // Civil
            "NNM24AI107"  // AI & DS
        })
        @DisplayName("F15 [Tier 2]: Non-CS department codes rejected")
        void testInvalidUsn_NonCsBranchCodes(String usn) {
            assertFalse(ValidationUtil.isValidUsn(usn), "Non-CS USN must be rejected: " + usn);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "NNM2CS107",    // 1-digit year
            "NNM2024CS107", // 4-digit year
            "NNMCS107",     // 0-digit year
            "NNM999CS107"   // 3-digit year
        })
        @DisplayName("F15 [Tier 2]: Incorrect year digit length rejected (must be exactly 2)")
        void testInvalidUsn_YearDigitCounts(String usn) {
            assertFalse(ValidationUtil.isValidUsn(usn), "Invalid year length must be rejected: " + usn);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "NNM24CS10",    // 2 digits
            "NNM24CS1",     // 1 digit
            "NNM24CS1078",  // 4 digits
            "NNM24CS10789"  // 5 digits
        })
        @DisplayName("F15 [Tier 2]: Incorrect roll number digit length rejected (must be exactly 3)")
        void testInvalidUsn_RollDigitCounts(String usn) {
            assertFalse(ValidationUtil.isValidUsn(usn), "Invalid roll number length must be rejected: " + usn);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {
            "   ",
            "\t\n",
            "NNM24CS10A", // alphanumeric roll
            "NNM24CS10!", // special character roll
            "1234567890"  // arbitrary digits
        })
        @DisplayName("F15 [Tier 2]: Null, empty, blank, and malformed strings rejected")
        void testInvalidUsn_NullEmptyAndSpecialCharacters(String usn) {
            assertFalse(ValidationUtil.isValidUsn(usn));
        }

        @Test
        @DisplayName("F15 [Tier 2]: Zero roll number boundary (NNM24CS000) accepted")
        void testValidUsn_RollNumberZeroBoundary() {
            assertTrue(ValidationUtil.isValidUsn("NNM24CS000"));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            " NNM24CS107",
            "NNM24CS107 ",
            " NNM24CS107 "
        })
        @DisplayName("F15 [Tier 2]: Untrimmed USN strings rejected by strict regex")
        void testInvalidUsn_WhitespacePadding(String usn) {
            assertFalse(ValidationUtil.isValidUsn(usn), "Untrimmed string must not match without trim: " + usn);
        }
    }

    // =========================================================================
    // 2. STUDENT NAME VALIDATION TESTS: ^[a-zA-Z\s.]+$ (Feature 16)
    // =========================================================================
    @Nested
    @DisplayName("Student Name Validation Regex (^[a-zA-Z\\s.]+$)")
    class StudentNameValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "Jeston Correa",
            "Jnanesh Karkera",
            "Ian Roniel Soans",
            "Dhanush U. Shetty",
            "Prof. Akhilraj V. Gadagkar",
            "Alice",
            "B."
        })
        @DisplayName("F16 [Tier 1]: Valid names with letters, spaces, and dots accepted")
        void testValidStudentNames(String name) {
            assertTrue(ValidationUtil.isValidStudentName(name), "Name should be valid: " + name);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "Jeston123",
            "12345",
            "007",
            "Student 2",
            "CS3603"
        })
        @DisplayName("F16 [Tier 2]: Names containing numeric digits rejected")
        void testInvalidStudentNames_ContainingDigits(String name) {
            assertFalse(ValidationUtil.isValidStudentName(name), "Digits must be rejected: " + name);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "Jnanesh_Karkera",
            "John@Doe",
            "Ian#Soans",
            "Dhanush$Shetty",
            "Name*Surname",
            "Mary-Jane" // Hyphen is not in [a-zA-Z\s.]
        })
        @DisplayName("F16 [Tier 2]: Names containing special symbols and hyphens rejected")
        void testInvalidStudentNames_SpecialSymbols(String name) {
            assertFalse(ValidationUtil.isValidStudentName(name), "Symbols must be rejected: " + name);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "<script>",
            "<img src=x>",
            "alert(1)",
            "<div>Test</div>"
        })
        @DisplayName("F16 [Tier 2]: Script and HTML injection payloads rejected")
        void testInvalidStudentNames_HtmlAndScriptTags(String name) {
            assertFalse(ValidationUtil.isValidStudentName(name), "HTML tags must be rejected: " + name);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {
            "   ",
            "\t\n\r"
        })
        @DisplayName("F16 [Tier 2]: Null, empty, and blank names rejected")
        void testInvalidStudentNames_NullAndBlank(String name) {
            assertFalse(ValidationUtil.isValidStudentName(name));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "A",
            "A. B. C. D.",
            "Dr. J. K. Row"
        })
        @DisplayName("F16 [Tier 2]: Single letter and multiple initials accepted")
        void testValidStudentNames_SingleLetterAndInitials(String name) {
            assertTrue(ValidationUtil.isValidStudentName(name));
        }
    }

    // =========================================================================
    // 3. LAB EXPERIMENT NUMBER VALIDATION (1 to 10 inclusive) (Feature 17)
    // =========================================================================
    @Nested
    @DisplayName("Lab Experiment Number Range (1 <= exp <= 10)")
    class LabExperimentNumberValidationTests {

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 5, 8, 9, 10})
        @DisplayName("F17 [Tier 1]: Valid integer experiment numbers (1 to 10) accepted")
        void testValidLabExpNumbers_IntegerPrimitives(int expNo) {
            assertTrue(ValidationUtil.isValidLabExpNo(expNo), "Exp " + expNo + " must be valid");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -5, -999})
        @DisplayName("F17 [Tier 2]: Numbers below lower bound (< 1) rejected")
        void testInvalidLabExpNumbers_BelowLowerBound(int expNo) {
            assertFalse(ValidationUtil.isValidLabExpNo(expNo), "Exp " + expNo + " must be rejected");
        }

        @ParameterizedTest
        @ValueSource(ints = {11, 12, 25, 100, 9999})
        @DisplayName("F17 [Tier 2]: Numbers above upper bound (> 10) rejected")
        void testInvalidLabExpNumbers_AboveUpperBound(int expNo) {
            assertFalse(ValidationUtil.isValidLabExpNo(expNo), "Exp " + expNo + " must be rejected");
        }

        @ParameterizedTest
        @CsvSource({
            "'1', true",
            "'5', true",
            "'10', true",
            "'0', false",
            "'-1', false",
            "'11', false",
            "'25', false"
        })
        @DisplayName("F17 [Tier 2]: String parsing of valid and invalid experiment boundaries")
        void testLabExpNumber_StringParsingValidAndInvalid(String input, boolean expected) {
            assertEquals(expected, ValidationUtil.isValidLabExpNo(input));
        }

        @Test
        @DisplayName("F17 [Tier 2]: Null string input handled safely")
        void testLabExpNumber_NullStringInputSafe() {
            assertFalse(ValidationUtil.isValidLabExpNo((String) null));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "abc",
            "1.5",
            "0x1",
            "NaN",
            "one",
            "10a"
        })
        @DisplayName("F17 [Tier 2]: Non-integer string inputs rejected without uncaught exception")
        void testLabExpNumber_NonIntegerStringRejections(String input) {
            assertFalse(ValidationUtil.isValidLabExpNo(input));
        }
    }

    // =========================================================================
    // 4. LANGUAGE WHITELIST VALIDATION: Java|JSP|SQL|HTML (Feature 18)
    // =========================================================================
    @Nested
    @DisplayName("Language Whitelist Validation (Java|JSP|SQL|HTML)")
    class LanguageWhitelistValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {"Java", "JSP", "SQL", "HTML"})
        @DisplayName("F18 [Tier 1]: Whitelisted languages accepted")
        void testValidLanguages(String lang) {
            assertTrue(ValidationUtil.isValidLanguage(lang), "Language must be valid: " + lang);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "java",
            "JAVA",
            "jsp",
            "Jsp",
            "sql",
            "html",
            "Html"
        })
        @DisplayName("F18 [Tier 2]: Case mismatch variations rejected")
        void testInvalidLanguages_CaseMismatch(String lang) {
            assertFalse(ValidationUtil.isValidLanguage(lang), "Case mismatch must be rejected: " + lang);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "Python",
            "C++",
            "C#",
            "Rust",
            "Go",
            "JavaScript",
            "TypeScript",
            "PHP",
            "Kotlin"
        })
        @DisplayName("F18 [Tier 2]: Unlisted programming languages rejected")
        void testInvalidLanguages_UnlistedLanguages(String lang) {
            assertFalse(ValidationUtil.isValidLanguage(lang), "Unlisted language must be rejected: " + lang);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {
            "   ",
            "Java ",
            " Java",
            "Java\n"
        })
        @DisplayName("F18 [Tier 2]: Null, empty, and padded strings rejected")
        void testInvalidLanguages_NullEmptyWhitespace(String lang) {
            assertFalse(ValidationUtil.isValidLanguage(lang));
        }
    }

    // =========================================================================
    // 5. EMAIL REGEX VALIDATION: ^[\w.-]+@[\w.-]+\.[a-zA-Z]{2,}$ (Feature 19)
    // =========================================================================
    @Nested
    @DisplayName("Email Format Regex Validation (^[\w.-]+@[\w.-]+\\.[a-zA-Z]{2,}$)")
    class EmailRegexValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "jeston@nmamit.in",
            "jnanesh.karkera@nmamit.in",
            "ian_soans@nitte.edu.in",
            "dhanush-shetty@gmail.com",
            "user123@domain.co.uk",
            "prof.akhilraj@nitte.edu.in"
        })
        @DisplayName("F19 [Tier 1]: Standard email formats accepted")
        void testValidEmailAddresses(String email) {
            assertTrue(ValidationUtil.isValidEmail(email), "Email must be valid: " + email);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "jeston",
            "jeston@",
            "@nmamit.in",
            "jeston@nmamit",
            "jeston@nmamit.",
            "plainaddress"
        })
        @DisplayName("F19 [Tier 2]: Incomplete email structures rejected")
        void testInvalidEmailAddresses_MissingParts(String email) {
            assertFalse(ValidationUtil.isValidEmail(email), "Incomplete email must be rejected: " + email);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "jeston@@nmamit.in",
            "jeston @nmamit.in",
            "jeston@ nmamit.in",
            "jeston@nmamit .in"
        })
        @DisplayName("F19 [Tier 2]: Double @ and embedded whitespace rejected")
        void testInvalidEmailAddresses_DoubleAtAndSpaces(String email) {
            assertFalse(ValidationUtil.isValidEmail(email));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "jeston@nmamit.c", // TLD only 1 char
            "jeston@nmamit.1", // TLD is numeric
            "jeston@nmamit.12"
        })
        @DisplayName("F19 [Tier 2]: Single-char and numeric TLD rejected ({2,} required)")
        void testInvalidEmailAddresses_ShortTld(String email) {
            assertFalse(ValidationUtil.isValidEmail(email));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t\n"})
        @DisplayName("F19 [Tier 2]: Null, empty, and whitespace emails rejected")
        void testInvalidEmailAddresses_NullAndBlank(String email) {
            assertFalse(ValidationUtil.isValidEmail(email));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "jeston.correa@sub.dept.nitte.edu.in",
            "student_lab.2024@cs.nmamit.ac.in"
        })
        @DisplayName("F19 [Tier 2]: Multi-level subdomains accepted")
        void testValidEmailAddresses_ComplexSubdomains(String email) {
            assertTrue(ValidationUtil.isValidEmail(email));
        }
    }

    // =========================================================================
    // 6. HTML ENTITY SANITIZATION & XSS PREVENTION (Feature 20)
    // =========================================================================
    @Nested
    @DisplayName("HTML Entity Sanitization (XSS Prevention)")
    class HtmlEntitySanitizationTests {

        @Test
        @DisplayName("F20 [Tier 1]: Sanitize individual entities: &, <, >, \", '")
        void testSanitizeIndividualEntities() {
            assertEquals("&amp;", SanitizationUtil.sanitizeHtml("&"));
            assertEquals("&lt;", SanitizationUtil.sanitizeHtml("<"));
            assertEquals("&gt;", SanitizationUtil.sanitizeHtml(">"));
            assertEquals("&quot;", SanitizationUtil.sanitizeHtml("\""));
            assertEquals("&#x27;", SanitizationUtil.sanitizeHtml("'"));
        }

        @Test
        @DisplayName("F20 [Tier 1]: Plain code without special characters remains unchanged")
        void testSanitizePlainTextUnchanged() {
            String plainCode = "public static void main(String[] args) {\n    int a = 10;\n    int b = 20;\n}";
            assertEquals(plainCode, SanitizationUtil.sanitizeHtml(plainCode));
        }

        @Test
        @DisplayName("F20 [Tier 2]: Neutralize script tag payload")
        void testSanitizeScriptTagPayload() {
            String raw = "<script>alert('xss')</script>";
            String expected = "&lt;script&gt;alert(&#x27;xss&#x27;)&lt;/script&gt;";
            assertEquals(expected, SanitizationUtil.sanitizeHtml(raw));
        }

        @Test
        @DisplayName("F20 [Tier 2]: Neutralize img onerror event handler payload")
        void testSanitizeImgOnErrorPayload() {
            String raw = "<img src=\"x\" onerror=\"alert('hack')\">";
            String expected = "&lt;img src=&quot;x&quot; onerror=&quot;alert(&#x27;hack&#x27;)&quot;&gt;";
            assertEquals(expected, SanitizationUtil.sanitizeHtml(raw));
        }

        @Test
        @DisplayName("F20 [Tier 2]: Verify replacement order prevents double-escaping (& converted first)")
        void testDoubleEscapingPrevention_AmpersandOrder() {
            // If < is converted to &lt; before &, then converting & to &amp; would produce &amp;lt;
            String raw = "foo & bar <tag>";
            String expected = "foo &amp; bar &lt;tag&gt;";
            assertEquals(expected, SanitizationUtil.sanitizeHtml(raw));

            // Already escaped HTML entity is neutralized cleanly
            String preEscaped = "&lt;script&gt;";
            String expectedDoubleEscaped = "&amp;lt;script&amp;gt;";
            assertEquals(expectedDoubleEscaped, SanitizationUtil.sanitizeHtml(preEscaped));
        }

        @Test
        @DisplayName("F20 [Tier 2]: Neutralize iframe with javascript: URI")
        void testSanitizeIframeWithJavascriptProtocol() {
            String raw = "<iframe src=\"javascript:alert(1)\"></iframe>";
            String expected = "&lt;iframe src=&quot;javascript:alert(1)&quot;&gt;&lt;/iframe&gt;";
            assertEquals(expected, SanitizationUtil.sanitizeHtml(raw));
        }

        @Test
        @DisplayName("F20 [Tier 2]: Neutralize SQL code with embedded quotes and ampersands")
        void testSanitizeSqlCodeWithQuotesAndAmpersands() {
            String raw = "SELECT * FROM users WHERE name = 'O\\'Reilly' & id = \"admin\";";
            String expected = "SELECT * FROM users WHERE name = &#x27;O\\&#x27;Reilly&#x27; &amp; id = &quot;admin&quot;;";
            assertEquals(expected, SanitizationUtil.sanitizeHtml(raw));
        }

        @Test
        @DisplayName("F20 [Tier 2]: Safe handling of null and empty inputs")
        void testSanitizeNullAndEmptyInputSafe() {
            assertEquals("", SanitizationUtil.sanitizeHtml(null), "Null input should return empty string without NPE");
            assertEquals("", SanitizationUtil.sanitizeHtml(""), "Empty input should return empty string");
        }

        @Test
        @DisplayName("F20 [Tier 2]: Neutralize HTML comments and orphan tags")
        void testSanitizeHtmlCommentsAndOrphanTags() {
            String raw = "<!-- comment --></div></span>";
            String expected = "&lt;!-- comment --&gt;&lt;/div&gt;&lt;/span&gt;";
            assertEquals(expected, SanitizationUtil.sanitizeHtml(raw));
        }

        @Test
        @DisplayName("F20 [Tier 2]: High-volume code block (20KB) sanitizes efficiently without memory blowup")
        void testSanitizeLargeCodeBlockPerformance() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 500; i++) {
                sb.append("if (x < ").append(i).append(" && y > 0) { str = \"tag_").append(i).append("'; } \n");
            }
            String largeCode = sb.toString();
            long start = System.currentTimeMillis();
            String sanitized = SanitizationUtil.sanitizeHtml(largeCode);
            long elapsed = System.currentTimeMillis() - start;

            assertNotNull(sanitized);
            assertFalse(sanitized.contains("<"));
            assertFalse(sanitized.contains(">"));
            assertTrue(elapsed < 200, "Sanitization of 20KB code should take < 200ms, took " + elapsed + "ms");
        }
    }

    // =========================================================================
    // 7. METADATA FORMATTING & PROCEDURAL VALIDATION (Exp 2a & 2b)
    // =========================================================================
    @Nested
    @DisplayName("Metadata Formatting & Procedural Validation List (Exp 2a & 2b)")
    class MetadataFormattingAndProceduralValidationTests {

        @Test
        @DisplayName("Exp 2a [Tier 1]: Format student author and USN composite string")
        void testFormatAuthorAndUsn() {
            assertEquals("Jeston Correa (NNM24CS107)", SanitizationUtil.formatAuthorAndUsn("Jeston Correa", "NNM24CS107"));
            assertEquals("Ian Roniel Soans (NNM24CS100)", SanitizationUtil.formatAuthorAndUsn("Ian Roniel Soans", "NNM24CS100"));
        }

        @Test
        @DisplayName("Exp 2a [Tier 1]: Format email to lowercase")
        void testFormatEmailToLowercase() {
            assertEquals("jeston@nmamit.in", SanitizationUtil.formatEmail("JESTON@NMAMIT.IN"));
            assertEquals("ian.soans@nitte.edu.in", SanitizationUtil.formatEmail("Ian.Soans@NITTE.EDU.IN"));
        }

        @Test
        @DisplayName("Exp 2b [Tier 1]: Non-empty check on code snippet and title")
        void testCodeAndTitleNonEmpty_Valid() {
            assertTrue(ValidationUtil.isNonEmpty("public class Test {}"));
            assertTrue(ValidationUtil.isNonEmpty("Lab Experiment 1 Title"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t\t\n", "\r\n"})
        @DisplayName("Exp 2b [Tier 2]: Blank and null code snippets rejected")
        void testCodeAndTitleNonEmpty_Invalid(String input) {
            assertFalse(ValidationUtil.isNonEmpty(input));
        }

        @Test
        @DisplayName("Exp 2a [Tier 2]: Null-safety on author and USN formatting")
        void testFormatAuthorAndUsn_NullSafety() {
            assertDoesNotThrow(() -> SanitizationUtil.formatAuthorAndUsn(null, null));
        }

        @Test
        @DisplayName("Exp 2a [Tier 2]: Null-safety on email lowercase formatting")
        void testFormatEmail_NullSafety() {
            assertEquals("", SanitizationUtil.formatEmail(null));
        }

        @Test
        @DisplayName("Exp 2b [Tier 2]: Comprehensive procedural validation captures all errors simultaneously")
        void testProceduralValidation_AllErrorsCapturedSimultaneously() {
            // Malformed USN, out-of-range expNo, invalid language, malformed email, blank code
            List<String> errors = ValidationUtil.validateSnippetSubmission(
                "invalid_usn",
                "Jeston_123",
                "bad_email@",
                "99",
                "Ruby",
                "",
                ""
            );

            assertNotNull(errors);
            assertTrue(errors.size() >= 6, "Expected at least 6 validation errors, got: " + errors.size());
            assertTrue(errors.stream().anyMatch(e -> e.contains("USN must follow valid format")));
            assertTrue(errors.stream().anyMatch(e -> e.contains("Name must contain only letters and spaces")));
            assertTrue(errors.stream().anyMatch(e -> e.contains("Lab Experiment No. must be a valid numeric value")));
            assertTrue(errors.stream().anyMatch(e -> e.contains("Invalid language selected")));
            assertTrue(errors.stream().anyMatch(e -> e.contains("Email must be in a valid format")));
            assertTrue(errors.stream().anyMatch(e -> e.contains("Code snippet cannot be empty")));
        }
    }
}
