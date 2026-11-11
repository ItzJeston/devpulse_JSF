package com.example.demo.repository;

import com.example.demo.entity.Snippet;
import com.example.demo.service.SnippetManager;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Adversarial Stress Test Suite for DevPulse JPA Persistence Layer.
 * Probes extreme edge cases, adversarial inputs, lob size limits,
 * Unicode preservation, nullability, and ID generation invariants.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AdversarialSnippetStressTest {

    @Autowired
    private SnippetRepository snippetRepository;

    @Autowired
    private SnippetManager snippetManager;

    @Autowired
    private EntityManager entityManager;

    private Snippet createValidSnippet() {
        Snippet s = new Snippet();
        s.setUsn("NNM24CS107");
        s.setStudentName("Jeston Correa");
        s.setEmail("jeston@nmamit.in");
        s.setLabExpNo(1);
        s.setLanguage("Java");
        s.setTitle("Adversarial Test Base");
        s.setRawCode("public class Test {}");
        s.setSanitizedCode("public class Test {}");
        s.setStatus("PENDING");
        s.setReviewerComments("Initial");
        s.setMarks(5);
        return s;
    }

    // =========================================================================
    // 1. MASSIVE LOB PAYLOADS (>15KB, >50KB, >100KB)
    // =========================================================================
    @Nested
    @DisplayName("Adversarial Lob Payload Stress Tests")
    class LobPayloadStressTests {

        @Test
        @DisplayName("Stress: 50,000 character code block in rawCode and sanitizedCode")
        void testFiftyKilobyteCodePayload() {
            StringBuilder sb = new StringBuilder();
            while (sb.length() < 50000) {
                sb.append("/* 50KB Lob payload chunk index=").append(sb.length()).append(" */\n")
                  .append("public void executeStress").append(sb.length()).append("() { int i = 42; }\n");
            }
            String code50k = sb.toString();
            assertTrue(code50k.length() >= 50000);

            Snippet s = createValidSnippet();
            s.setTitle("50KB Payload");
            s.setRawCode(code50k);
            s.setSanitizedCode(code50k);

            Snippet saved = snippetRepository.save(s);
            Long id = saved.getId();
            entityManager.flush();
            entityManager.clear();

            Snippet reloaded = snippetRepository.findById(id).orElseThrow();
            assertNotNull(reloaded.getRawCode());
            assertEquals(code50k.length(), reloaded.getRawCode().length());
            assertEquals(code50k, reloaded.getRawCode());
            assertEquals(code50k, reloaded.getSanitizedCode());
        }

        @Test
        @DisplayName("Stress: 100,000 character code block in rawCode and sanitizedCode")
        void testOneHundredKilobyteCodePayload() {
            StringBuilder sb = new StringBuilder();
            while (sb.length() < 100000) {
                sb.append("var block_").append(sb.length()).append(" = 'abcdefghijklmnopqrstuvwxyz0123456789';\n");
            }
            String code100k = sb.toString();
            assertTrue(code100k.length() >= 100000);

            Snippet s = createValidSnippet();
            s.setTitle("100KB Payload");
            s.setRawCode(code100k);
            s.setSanitizedCode(code100k);

            Snippet saved = snippetManager.saveSnippet(s);
            Long id = saved.getId();
            entityManager.flush();
            entityManager.clear();

            Snippet reloaded = snippetManager.getSnippetById(id);
            assertNotNull(reloaded.getRawCode());
            assertEquals(code100k.length(), reloaded.getRawCode().length());
            assertEquals(code100k, reloaded.getRawCode());
        }
    }

    // =========================================================================
    // 2. EXTREME UNICODE, SPECIAL CHARACTERS, SURROGATE PAIRS, MULTIBYTE
    // =========================================================================
    @Nested
    @DisplayName("Adversarial Character Encoding & Unicode Tests")
    class UnicodeAndEncodingTests {

        @Test
        @DisplayName("Stress: Multi-byte Unicode (Emojis, CJK, Arabic RTL, Math symbols)")
        void testMultibyteUnicodeAndEmojis() {
            String extremeUnicode = "🚀🎉 Java Spring 🔥✨\n" +
                                    "// Chinese: 你好世界，实验室代码\n" +
                                    "// Japanese: こんにちは世界\n" +
                                    "// Arabic (RTL): مرحبا بالعالم\n" +
                                    "// Math: ∀x ∈ ℝ, ∃y : x + y = 0 ∧ ∑(i=1..n) i² = n(n+1)(2n+1)/6\n" +
                                    "// Currency: € £ ¥ ₹ ₿\n" +
                                    "// Quotes & Slashes: \"hello\" 'world' `backtick` \\ / // /* */ \r\n \t \f";

            Snippet s = createValidSnippet();
            s.setTitle("Unicode Stress 🌟");
            s.setRawCode(extremeUnicode);
            s.setSanitizedCode(extremeUnicode);

            Snippet saved = snippetRepository.save(s);
            Long id = saved.getId();
            entityManager.flush();
            entityManager.clear();

            Snippet reloaded = snippetRepository.findById(id).orElseThrow();
            assertEquals(extremeUnicode, reloaded.getRawCode());
            assertEquals(extremeUnicode, reloaded.getSanitizedCode());
            assertEquals("Unicode Stress 🌟", reloaded.getTitle());
        }

        @Test
        @DisplayName("Stress: SQL Injection & XSS payload strings in code and title")
        void testSqlInjectionAndXssStringIntegrity() {
            String sqlInjectionPayload = "'; DROP TABLE snippet; -- ' OR '1'='1'";
            String xssPayload = "<script>alert('XSS Attack!');</script><img src=x onerror=alert(1)>";

            Snippet s = createValidSnippet();
            s.setTitle("SQL/XSS Attack Title");
            s.setRawCode(sqlInjectionPayload);
            s.setSanitizedCode(xssPayload);

            Snippet saved = snippetRepository.save(s);
            Long id = saved.getId();
            entityManager.flush();
            entityManager.clear();

            Snippet reloaded = snippetRepository.findById(id).orElseThrow();
            // Data must be saved verbatim without interpreting SQL or script execution
            assertEquals(sqlInjectionPayload, reloaded.getRawCode());
            assertEquals(xssPayload, reloaded.getSanitizedCode());

            // Ensure database tables still exist
            assertTrue(snippetRepository.count() > 0);
        }
    }

    // =========================================================================
    // 3. NULLABILITY & ZERO-LENGTH BOUNDARIES
    // =========================================================================
    @Nested
    @DisplayName("Nullability and Boundary Tests")
    class NullabilityBoundaryTests {

        @Test
        @DisplayName("Stress: All nullable string fields set to null")
        void testAllNullableFieldsNull() {
            Snippet s = new Snippet();
            s.setUsn(null);
            s.setStudentName(null);
            s.setEmail(null);
            s.setLabExpNo(0);
            s.setLanguage(null);
            s.setTitle(null);
            s.setRawCode(null);
            s.setSanitizedCode(null);
            s.setStatus(null);
            s.setReviewerComments(null);
            s.setMarks(0);

            Snippet saved = snippetRepository.save(s);
            Long id = saved.getId();
            entityManager.flush();
            entityManager.clear();

            Snippet reloaded = snippetRepository.findById(id).orElseThrow();
            assertNull(reloaded.getUsn());
            assertNull(reloaded.getStudentName());
            assertNull(reloaded.getEmail());
            assertNull(reloaded.getRawCode());
            assertNull(reloaded.getSanitizedCode());
            assertNull(reloaded.getReviewerComments());
        }

        @Test
        @DisplayName("Stress: Empty string (0-length) for all text fields")
        void testEmptyStringFields() {
            Snippet s = new Snippet();
            s.setUsn("");
            s.setStudentName("");
            s.setEmail("");
            s.setLabExpNo(0);
            s.setLanguage("");
            s.setTitle("");
            s.setRawCode("");
            s.setSanitizedCode("");
            s.setStatus("");
            s.setReviewerComments("");
            s.setMarks(0);

            Snippet saved = snippetRepository.save(s);
            Long id = saved.getId();
            entityManager.flush();
            entityManager.clear();

            Snippet reloaded = snippetRepository.findById(id).orElseThrow();
            assertEquals("", reloaded.getUsn());
            assertEquals("", reloaded.getRawCode());
            assertEquals("", reloaded.getSanitizedCode());
            assertEquals("", reloaded.getReviewerComments());
        }

        @Test
        @DisplayName("Stress: labExpNo boundaries (0, 1, 10, 11, -1, Integer.MAX_VALUE)")
        void testLabExpNoIntegerBoundaries() {
            int[] testValues = {0, 1, 10, 11, -1, 100, Integer.MAX_VALUE, Integer.MIN_VALUE};
            for (int val : testValues) {
                Snippet s = createValidSnippet();
                s.setLabExpNo(val);
                Snippet saved = snippetRepository.save(s);
                entityManager.flush();
                entityManager.clear();

                Snippet reloaded = snippetRepository.findById(saved.getId()).orElseThrow();
                assertEquals(val, reloaded.getLabExpNo());
            }
        }

        @Test
        @DisplayName("Stress: marks boundaries (0, 10, -1, 100, Integer.MAX_VALUE)")
        void testMarksIntegerBoundaries() {
            int[] testMarks = {0, 10, -1, 100, Integer.MAX_VALUE, Integer.MIN_VALUE};
            for (int mark : testMarks) {
                Snippet s = createValidSnippet();
                s.setMarks(mark);
                Snippet saved = snippetRepository.save(s);
                entityManager.flush();
                entityManager.clear();

                Snippet reloaded = snippetRepository.findById(saved.getId()).orElseThrow();
                assertEquals(mark, reloaded.getMarks());
            }
        }
    }

    // =========================================================================
    // 4. ID GENERATION & NON-EXISTENT ID CONTRACT INVARIANTS
    // =========================================================================
    @Nested
    @DisplayName("ID Generation and Query Invariants")
    class IdGenerationAndQueryInvariants {

        @Test
        @DisplayName("Stress: snippetManager.getSnippetById(null) throws NoSuchElementException")
        void testGetSnippetByIdNullThrowsNoSuchElementException() {
            assertThrows(NoSuchElementException.class, () -> snippetManager.getSnippetById(null));
        }

        @Test
        @DisplayName("Stress: snippetManager.exists(null) returns false")
        void testExistsNullReturnsFalse() {
            assertFalse(snippetManager.exists(null));
        }

        @Test
        @DisplayName("Stress: snippetManager.deleteSnippet(null) is safe no-op")
        void testDeleteSnippetNullIsSafe() {
            assertDoesNotThrow(() -> snippetManager.deleteSnippet(null));
        }

        @Test
        @DisplayName("Stress: snippetManager.deleteSnippet(-999L) is safe no-op")
        void testDeleteSnippetNegativeIsSafe() {
            assertDoesNotThrow(() -> snippetManager.deleteSnippet(-999L));
        }

        @Test
        @DisplayName("Stress: snippetManager.saveSnippet(null) throws IllegalArgumentException")
        void testSaveSnippetNullThrowsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () -> snippetManager.saveSnippet(null));
        }

        @Test
        @DisplayName("Stress: Non-existent IDs (-1L, 0L, Long.MAX_VALUE, Long.MIN_VALUE) via manager and repo")
        void testNonExistentIdsAcrossLayer() {
            Long[] nonExistent = {-1L, 0L, 9999999L, Long.MAX_VALUE, Long.MIN_VALUE};
            for (Long badId : nonExistent) {
                assertFalse(snippetManager.exists(badId), "exists should be false for: " + badId);
                assertFalse(snippetRepository.existsById(badId), "repo existsById should be false for: " + badId);
                assertThrows(NoSuchElementException.class, () -> snippetManager.getSnippetById(badId));
                assertTrue(snippetRepository.findById(badId).isEmpty());
            }
        }

        @Test
        @DisplayName("Stress: Monotonically increasing auto-generated identity IDs")
        void testMonotonicallyIncreasingIdentityIds() {
            Snippet s1 = snippetRepository.save(createValidSnippet());
            Snippet s2 = snippetRepository.save(createValidSnippet());
            Snippet s3 = snippetRepository.save(createValidSnippet());
            entityManager.flush();

            assertNotNull(s1.getId());
            assertNotNull(s2.getId());
            assertNotNull(s3.getId());
            assertTrue(s2.getId() > s1.getId(), "IDs must increase monotonically");
            assertTrue(s3.getId() > s2.getId(), "IDs must increase monotonically");
        }
    }
}
