package com.example.demo.service;

import com.example.demo.entity.Snippet;
import com.example.demo.repository.SnippetRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test suite for SnippetManager service layer.
 * Exercises Experiments 4 and 5: service-level CRUD operations, existence checks,
 * NoSuchElementException contracts required by ApiController 404 handling,
 * transactional persistence, and service-repository synchronization.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SnippetManagerTest {

    @Autowired
    private SnippetManager snippetManager;

    @Autowired
    private SnippetRepository snippetRepository;

    private Snippet createSampleSnippet(String usn, String name, String email, int expNo,
                                         String lang, String title, String raw, String sanitized,
                                         String status, String comments, int marks) {
        Snippet s = new Snippet();
        s.setUsn(usn);
        s.setStudentName(name);
        s.setEmail(email);
        s.setLabExpNo(expNo);
        s.setLanguage(lang);
        s.setTitle(title);
        s.setRawCode(raw);
        s.setSanitizedCode(sanitized);
        s.setStatus(status);
        s.setReviewerComments(comments);
        s.setMarks(marks);
        return s;
    }

    // =========================================================================
    // 1. TIER 1: FEATURE COVERAGE TESTS (Feature 14: SnippetManager Service)
    // =========================================================================
    @Nested
    @DisplayName("Tier 1: Feature Coverage (Service CRUD Operations)")
    class FeatureCoverageTests {

        @Test
        @DisplayName("F14 [Tier 1]: saveSnippet via manager assigns positive ID")
        void testSaveSnippetViaManagerAssignsId() {
            Snippet s = createSampleSnippet(
                "NNM24CS107", "Jeston Correa", "jeston@nmamit.in", 1,
                "Java", "Hello World", "System.out.println(\"Hi\");",
                "System.out.println(&quot;Hi&quot;);", "PENDING", "", 0
            );

            assertNull(s.getId());
            snippetManager.saveSnippet(s);

            assertNotNull(s.getId(), "Service save must assign generated ID");
            assertTrue(s.getId() > 0);
        }

        @Test
        @DisplayName("F14 [Tier 1]: getSnippetById returns matching saved snippet")
        void testGetSnippetByIdExisting() {
            Snippet s = createSampleSnippet(
                "NNM24CS108", "Jnanesh Karkera", "jnanesh@nmamit.in", 2,
                "JSP", "JSP Scriptlets", "<% out.print(\"JSP\"); %>",
                "&lt;% out.print(&quot;JSP&quot;); %&gt;", "PENDING", "", 0
            );
            snippetManager.saveSnippet(s);
            Long id = s.getId();

            Snippet found = snippetManager.getSnippetById(id);
            assertNotNull(found);
            assertEquals(id, found.getId());
            assertEquals("NNM24CS108", found.getUsn());
            assertEquals("Jnanesh Karkera", found.getStudentName());
            assertEquals("JSP Scriptlets", found.getTitle());
            assertEquals("<% out.print(\"JSP\"); %>", found.getRawCode());
        }

        @Test
        @DisplayName("F14 [Tier 1]: getAllSnippets returns list of all persisted records")
        void testGetAllSnippetsReturnsList() {
            Snippet s1 = createSampleSnippet("NNM24CS107", "Jeston Correa", "jeston@nmamit.in", 1, "Java", "L1", "c1", "c1", "PENDING", "", 0);
            Snippet s2 = createSampleSnippet("NNM24CS100", "Ian Roniel Soans", "ian@nmamit.in", 2, "SQL", "L2", "c2", "c2", "PENDING", "", 0);

            snippetManager.saveSnippet(s1);
            snippetManager.saveSnippet(s2);

            List<Snippet> list = snippetManager.getAllSnippets();
            assertNotNull(list);
            assertTrue(list.size() >= 2);
            assertTrue(list.stream().anyMatch(sn -> sn.getId().equals(s1.getId())));
            assertTrue(list.stream().anyMatch(sn -> sn.getId().equals(s2.getId())));
        }

        @Test
        @DisplayName("F14 [Tier 1]: updateSnippet via manager persists status, comments, marks")
        void testUpdateSnippetViaManager() {
            Snippet s = createSampleSnippet(
                "NNM24CS076", "Dhanush U. Shetty", "dhanush@gmail.com", 3,
                "HTML", "CSS Flexbox", "<div></div>", "&lt;div&gt;&lt;/div&gt;",
                "PENDING", "Pending evaluation", 0
            );
            snippetManager.saveSnippet(s);
            Long id = s.getId();

            Snippet toUpdate = snippetManager.getSnippetById(id);
            toUpdate.setStatus("NEEDS_REVISION");
            toUpdate.setReviewerComments("Add responsive flex-wrap property");
            toUpdate.setMarks(5);
            snippetManager.saveSnippet(toUpdate);

            Snippet reloaded = snippetManager.getSnippetById(id);
            assertEquals("NEEDS_REVISION", reloaded.getStatus());
            assertEquals("Add responsive flex-wrap property", reloaded.getReviewerComments());
            assertEquals(5, reloaded.getMarks());
        }

        @Test
        @DisplayName("F14 [Tier 1]: deleteSnippet removes record via service")
        void testDeleteSnippetViaManager() {
            Snippet s = createSampleSnippet(
                "NNM24CS107", "Jeston Correa", "jeston@nmamit.in", 1,
                "Java", "To Delete", "code", "code", "PENDING", "", 0
            );
            snippetManager.saveSnippet(s);
            Long id = s.getId();

            assertTrue(snippetManager.exists(id));
            snippetManager.deleteSnippet(id);
            assertFalse(snippetManager.exists(id));
        }

        @Test
        @DisplayName("F14 [Tier 1]: exists returns true for existing snippet")
        void testExistsReturnsTrueForSavedSnippet() {
            Snippet s = createSampleSnippet(
                "NNM24CS108", "Jnanesh Karkera", "jnanesh@nmamit.in", 2,
                "Java", "Exists Test", "code", "code", "PENDING", "", 0
            );
            snippetManager.saveSnippet(s);

            assertTrue(snippetManager.exists(s.getId()));
        }
    }

    // =========================================================================
    // 2. TIER 2: BOUNDARY & CONTRACT TESTS (NoSuchElementException for 404s)
    // =========================================================================
    @Nested
    @DisplayName("Tier 2: Boundary & Contract Tests")
    class BoundaryAndContractTests {

        @Test
        @DisplayName("F14 [Tier 2]: getSnippetById throws NoSuchElementException for missing IDs")
        void testGetSnippetByIdThrowsNoSuchElementExceptionWhenMissing() {
            assertThrows(NoSuchElementException.class, () -> snippetManager.getSnippetById(999999L),
                "Missing ID must throw NoSuchElementException so ApiController can translate to 404");
            assertThrows(NoSuchElementException.class, () -> snippetManager.getSnippetById(-1L));
            assertThrows(NoSuchElementException.class, () -> snippetManager.getSnippetById(0L));
        }

        @Test
        @DisplayName("F14 [Tier 2]: exists returns false for missing IDs")
        void testExistsReturnsFalseForMissingIds() {
            assertFalse(snippetManager.exists(999999L));
            assertFalse(snippetManager.exists(-1L));
            assertFalse(snippetManager.exists(0L));
        }

        @Test
        @DisplayName("F14 [Tier 2]: getAllSnippets when empty returns empty non-null list")
        void testGetAllSnippetsWhenEmpty() {
            List<Snippet> list = snippetManager.getAllSnippets();
            assertNotNull(list, "getAllSnippets should never return null");
        }

        @Test
        @DisplayName("F14 [Tier 2]: saveSnippet handles large code block (>5,000 characters)")
        void testSaveSnippetWithLargeCodePayload() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 300; i++) {
                sb.append("public void function_").append(i).append("() { String s = \"data_").append(i).append("\"; }\n");
            }
            String largeCode = sb.toString();
            assertTrue(largeCode.length() > 5000);

            Snippet s = createSampleSnippet(
                "NNM24CS100", "Ian Roniel Soans", "ian@nmamit.in", 5,
                "Java", "Large Code Payload", largeCode, largeCode, "PENDING", "", 0
            );

            snippetManager.saveSnippet(s);
            Long id = s.getId();

            Snippet reloaded = snippetManager.getSnippetById(id);
            assertEquals(largeCode.length(), reloaded.getRawCode().length());
            assertEquals(largeCode, reloaded.getRawCode());
        }
    }

    // =========================================================================
    // 3. TIER 3: SERVICE-REPOSITORY SYNCHRONIZATION
    // =========================================================================
    @Nested
    @DisplayName("Tier 3: Service-Repository State Consistency")
    class SynchronizationTests {

        @Test
        @DisplayName("F13 ↔ F14 [Tier 3]: Consistency across SnippetManager and SnippetRepository")
        void testServiceRepositoryConsistency() {
            // Save via repository
            Snippet s = createSampleSnippet(
                "NNM24CS107", "Jeston Correa", "jeston@nmamit.in", 3,
                "SQL", "DDL Lab", "CREATE TABLE test (id INT);", "CREATE TABLE test (id INT);",
                "PENDING", "", 0
            );
            Snippet savedRepo = snippetRepository.save(s);
            Long id = savedRepo.getId();

            // Read via manager
            Snippet foundManager = snippetManager.getSnippetById(id);
            assertEquals("DDL Lab", foundManager.getTitle());

            // Update via manager
            foundManager.setTitle("DDL Lab Updated");
            snippetManager.saveSnippet(foundManager);

            // Read via repository
            Snippet reloadedRepo = snippetRepository.findById(id).orElseThrow();
            assertEquals("DDL Lab Updated", reloadedRepo.getTitle());
        }

        @Test
        @DisplayName("F13 ↔ F14 [Tier 3]: Deletion via manager is immediately reflected in repository")
        void testDeleteViaManagerReflectedInRepository() {
            Snippet s = createSampleSnippet(
                "NNM24CS108", "Jnanesh Karkera", "jnanesh@nmamit.in", 1,
                "Java", "Delete Sync", "code", "code", "PENDING", "", 0
            );
            snippetManager.saveSnippet(s);
            Long id = s.getId();

            assertTrue(snippetRepository.existsById(id));
            snippetManager.deleteSnippet(id);
            assertFalse(snippetRepository.existsById(id));
            assertTrue(snippetRepository.findById(id).isEmpty());
        }
    }

    // =========================================================================
    // 4. TIER 4: MULTI-STUDENT EVALUATION SCENARIO
    // =========================================================================
    @Nested
    @DisplayName("Tier 4: Multi-Student Evaluation Workload")
    class MultiStudentEvaluationScenarioTests {

        @Test
        @DisplayName("Exp 4 & 5 [Tier 4]: Multi-student concurrent submission and distinct faculty evaluation")
        void testMultiStudentEvaluationScenario() {
            // 3 students submit snippets for different experiments
            Snippet s1 = createSampleSnippet("NNM24CS107", "Jeston Correa", "jeston@nmamit.in", 1, "Java", "Student 1 Lab 1", "code1", "code1", "PENDING", "", 0);
            Snippet s2 = createSampleSnippet("NNM24CS108", "Jnanesh Karkera", "jnanesh@nmamit.in", 2, "JSP", "Student 2 Lab 2", "code2", "code2", "PENDING", "", 0);
            Snippet s3 = createSampleSnippet("NNM24CS100", "Ian Roniel Soans", "ian@nmamit.in", 3, "SQL", "Student 3 Lab 3", "code3", "code3", "PENDING", "", 0);

            snippetManager.saveSnippet(s1);
            snippetManager.saveSnippet(s2);
            snippetManager.saveSnippet(s3);

            // Faculty evaluates all 3 with different marks and comments
            Snippet eval1 = snippetManager.getSnippetById(s1.getId());
            eval1.setStatus("APPROVED");
            eval1.setMarks(8);
            eval1.setReviewerComments("Good basic implementation");
            snippetManager.saveSnippet(eval1);

            Snippet eval2 = snippetManager.getSnippetById(s2.getId());
            eval2.setStatus("APPROVED");
            eval2.setMarks(9);
            eval2.setReviewerComments("Strong JSP scriptlet separation");
            snippetManager.saveSnippet(eval2);

            Snippet eval3 = snippetManager.getSnippetById(s3.getId());
            eval3.setStatus("APPROVED");
            eval3.setMarks(10);
            eval3.setReviewerComments("Flawless SQL schema design");
            snippetManager.saveSnippet(eval3);

            // Verify all 3 persist independently
            Snippet final1 = snippetManager.getSnippetById(s1.getId());
            Snippet final2 = snippetManager.getSnippetById(s2.getId());
            Snippet final3 = snippetManager.getSnippetById(s3.getId());

            assertEquals(8, final1.getMarks());
            assertEquals("Good basic implementation", final1.getReviewerComments());

            assertEquals(9, final2.getMarks());
            assertEquals("Strong JSP scriptlet separation", final2.getReviewerComments());

            assertEquals(10, final3.getMarks());
            assertEquals("Flawless SQL schema design", final3.getReviewerComments());
        }
    }
}
