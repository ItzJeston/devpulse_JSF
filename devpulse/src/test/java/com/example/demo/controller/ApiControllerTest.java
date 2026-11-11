package com.example.demo.controller;

import com.example.demo.entity.Snippet;
import com.example.demo.repository.SnippetRepository;
import com.example.demo.service.SnippetManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test suite for ApiController (Experiment 5 REST API Layer).
 * Covers Tier 1 (nominal), Tier 2 (boundary & negative), Tier 3 (cross-feature),
 * and Tier 4 (full real-world workload scenarios).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SnippetManager snippetManager;

    @Autowired
    private SnippetRepository snippetRepository;

    @BeforeEach
    void setUp() {
        snippetRepository.deleteAll();
    }

    private Snippet buildSnippet(String usn, String name, String email, int expNo,
                                 String language, String title, String rawCode) {
        Snippet s = new Snippet();
        s.setUsn(usn);
        s.setStudentName(name);
        s.setEmail(email);
        s.setLabExpNo(expNo);
        s.setLanguage(language);
        s.setTitle(title);
        s.setRawCode(rawCode);
        s.setSanitizedCode(rawCode);
        s.setStatus("PENDING");
        s.setReviewerComments("");
        s.setMarks(0);
        return s;
    }

    private Snippet persistSnippet(String usn, String name, String email, int expNo,
                                   String language, String title, String rawCode) {
        Snippet s = buildSnippet(usn, name, email, expNo, language, title, rawCode);
        snippetManager.saveSnippet(s);
        return s;
    }

    // =========================================================================
    // TIER 1: FEATURE COVERAGE (Nominal CRUD Endpoints)
    // =========================================================================
    @Nested
    @DisplayName("Tier 1: Nominal REST API Endpoints")
    class Tier1FeatureCoverageTests {

        @Test
        @DisplayName("F29 [Tier 1]: GET /api/snippets returns 200 OK and JSON array")
        void testListAllSnippetsReturns200OkAndJsonArray() throws Exception {
            persistSnippet("NNM24CS107", "Jeston Correa", "jeston@nmamit.in", 1, "Java", "Exp 1", "System.out.println(1);");
            persistSnippet("NNM24CS108", "Jnanesh Karkera", "jnanesh@nmamit.in", 2, "JSP", "Exp 2", "<% int x = 2; %>");

            mockMvc.perform(get("/api/snippets"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(2)))
                    .andExpect(jsonPath("$[0].usn").exists())
                    .andExpect(jsonPath("$[0].title").exists());
        }

        @Test
        @DisplayName("F30 [Tier 1]: GET /api/snippets/{id} found returns 200 OK and Snippet JSON")
        void testGetSnippetByIdFoundReturns200OkAndSnippetJson() throws Exception {
            Snippet s = persistSnippet("NNM24CS107", "Jeston Correa", "jeston@nmamit.in", 4, "Java", "QuickSort", "int x = 1;");

            mockMvc.perform(get("/api/snippets/" + s.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(s.getId()))
                    .andExpect(jsonPath("$.usn").value("NNM24CS107"))
                    .andExpect(jsonPath("$.studentName").value("Jeston Correa"))
                    .andExpect(jsonPath("$.title").value("QuickSort"))
                    .andExpect(jsonPath("$.labExpNo").value(4));
        }

        @Test
        @DisplayName("F30 [Tier 1]: GET /api/snippets/{id} not found returns 404 NOT FOUND")
        void testGetSnippetByIdNotFoundReturns404NotFound() throws Exception {
            mockMvc.perform(get("/api/snippets/999999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("F31 [Tier 1]: POST /api/snippets creates snippet and auto-sanitizes raw code")
        void testCreateSnippetPostReturns200Or201AndAutoSanitizesCode() throws Exception {
            Snippet payload = buildSnippet("NNM24CS108", "Jnanesh Karkera", "jnanesh@nmamit.in", 5,
                    "Java", "REST API Lab", "<script>alert('XSS')</script>\nString val = \"demo\" & \"test\";");

            String json = objectMapper.writeValueAsString(payload);

            mockMvc.perform(post("/api/snippets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().is2xxSuccessful());

            List<Snippet> list = snippetRepository.findAll();
            assertFalse(list.isEmpty(), "Persisted snippet list must not be empty");

            Snippet saved = list.stream()
                    .filter(sn -> "NNM24CS108".equals(sn.getUsn()))
                    .findFirst()
                    .orElse(null);

            assertNotNull(saved, "Snippet must be saved in database");
            assertEquals("<script>alert('XSS')</script>\nString val = \"demo\" & \"test\";", saved.getRawCode());
            assertTrue(saved.getSanitizedCode().contains("&lt;script&gt;alert(&#x27;XSS&#x27;)&lt;/script&gt;"),
                    "sanitizedCode must escape script tags and single quotes");
            assertTrue(saved.getSanitizedCode().contains("&quot;demo&quot; &amp; &quot;test&quot;"),
                    "sanitizedCode must escape double quotes and ampersands");
            assertEquals("PENDING", saved.getStatus());
        }

        @Test
        @DisplayName("F32 [Tier 1]: PUT /api/snippets/{id} updates snippet and returns 200 OK")
        void testUpdateSnippetPutReturns200OkWhenFound() throws Exception {
            Snippet s = persistSnippet("NNM24CS107", "Jeston Correa", "jeston@nmamit.in", 1, "Java", "Initial", "code");

            s.setStatus("APPROVED");
            s.setMarks(9);
            s.setReviewerComments("Verified lab output.");
            String json = objectMapper.writeValueAsString(s);

            mockMvc.perform(put("/api/snippets/" + s.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk());

            Snippet updated = snippetManager.getSnippetById(s.getId());
            assertEquals("APPROVED", updated.getStatus());
            assertEquals(9, updated.getMarks());
            assertEquals("Verified lab output.", updated.getReviewerComments());
        }

        @Test
        @DisplayName("F33 [Tier 1]: DELETE /api/snippets/{id} found returns 200 OK")
        void testDeleteSnippetFoundReturns200Ok() throws Exception {
            Snippet s = persistSnippet("NNM24CS100", "Ian Roniel Soans", "ian@nmamit.in", 3, "SQL", "SQL Lab", "SELECT 1;");

            mockMvc.perform(delete("/api/snippets/" + s.getId()))
                    .andExpect(status().isOk());

            assertFalse(snippetManager.exists(s.getId()), "Snippet must be deleted from repository");

            mockMvc.perform(get("/api/snippets/" + s.getId()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("F33 [Tier 1]: DELETE /api/snippets/{id} not found returns 404 NOT FOUND")
        void testDeleteSnippetNotFoundReturns404NotFound() throws Exception {
            mockMvc.perform(delete("/api/snippets/999999"))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // TIER 2: BOUNDARY & CORNER CASES
    // =========================================================================
    @Nested
    @DisplayName("Tier 2: Boundary & Corner Cases")
    class Tier2BoundaryAndCornerTests {

        @Test
        @DisplayName("F30 [Tier 2]: GET /api/snippets with negative and zero IDs returns 404")
        void testGetSnippetByIdWithNegativeAndZeroIdReturns404() throws Exception {
            mockMvc.perform(get("/api/snippets/-1"))
                    .andExpect(status().isNotFound());

            mockMvc.perform(get("/api/snippets/0"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("F30 [Tier 2]: GET /api/snippets with non-numeric ID returns 400 Bad Request")
        void testGetSnippetByIdWithNonNumericIdReturns400BadRequest() throws Exception {
            mockMvc.perform(get("/api/snippets/invalid-id-abc"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("F29 [Tier 2]: GET /api/snippets when empty returns empty JSON array")
        void testListAllSnippetsWhenEmptyReturnsEmptyArray() throws Exception {
            snippetRepository.deleteAll();

            mockMvc.perform(get("/api/snippets"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("F32 [Tier 2]: PUT /api/snippets/{id} with non-existent ID returns 404")
        void testPutSnippetNotFoundReturns404() throws Exception {
            Snippet s = buildSnippet("NNM24CS076", "Dhanush U. Shetty", "dhanush@nmamit.in", 4, "Java", "Missing", "code");
            s.setId(999999L);
            String json = objectMapper.writeValueAsString(s);

            mockMvc.perform(put("/api/snippets/999999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("F33 [Tier 2]: DELETE /api/snippets/{id} twice returns 404 on second attempt")
        void testDeleteSnippetTwiceReturns404OnSecondAttempt() throws Exception {
            Snippet s = persistSnippet("NNM24CS107", "Jeston Correa", "jeston@nmamit.in", 1, "Java", "Delete Twice", "code");

            mockMvc.perform(delete("/api/snippets/" + s.getId()))
                    .andExpect(status().isOk());

            mockMvc.perform(delete("/api/snippets/" + s.getId()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("F31 [Tier 2]: POST /api/snippets handles large code payload (>5000 chars)")
        void testPostSnippetWithLargeCodePayload() throws Exception {
            StringBuilder largeCode = new StringBuilder();
            for (int i = 0; i < 200; i++) {
                largeCode.append("// Line ").append(i).append(": public void process() { int val = ").append(i).append("; }\n");
            }
            assertTrue(largeCode.length() > 5000, "Code string must exceed 5000 characters");

            Snippet payload = buildSnippet("NNM24CS107", "Jeston Correa", "jeston@nmamit.in", 2, "Java", "Large Code", largeCode.toString());
            String json = objectMapper.writeValueAsString(payload);

            mockMvc.perform(post("/api/snippets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().is2xxSuccessful());

            List<Snippet> list = snippetRepository.findAll();
            Snippet saved = list.stream().filter(sn -> "Large Code".equals(sn.getTitle())).findFirst().orElse(null);
            assertNotNull(saved);
            assertEquals(largeCode.toString(), saved.getRawCode());
            assertTrue(saved.getRawCode().length() > 5000);
        }

        @Test
        @DisplayName("F31 [Tier 2]: POST /api/snippets handles complex special characters and quotes")
        void testPostSnippetWithSpecialCharactersAndQuotes() throws Exception {
            String complexCode = "String q = \"hello\" & 'world' <tag> > 0;";
            Snippet payload = buildSnippet("NNM24CS108", "Jnanesh Karkera", "jnanesh@nmamit.in", 3, "Java", "Quotes Test", complexCode);
            String json = objectMapper.writeValueAsString(payload);

            mockMvc.perform(post("/api/snippets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().is2xxSuccessful());

            List<Snippet> list = snippetRepository.findAll();
            Snippet saved = list.stream().filter(sn -> "Quotes Test".equals(sn.getTitle())).findFirst().orElse(null);
            assertNotNull(saved);
            assertEquals(complexCode, saved.getRawCode());
            assertEquals("String q = &quot;hello&quot; &amp; &#x27;world&#x27; &lt;tag&gt; &gt; 0;", saved.getSanitizedCode());
        }

        @Test
        @DisplayName("F31 [Tier 2]: POST /api/snippets defaults status to PENDING if omitted")
        void testPostSnippetWithoutExplicitStatusDefaultsToPending() throws Exception {
            Snippet payload = buildSnippet("NNM24CS100", "Ian Roniel Soans", "ian@nmamit.in", 1, "Java", "No Status", "System.out.println();");
            payload.setStatus(null);
            String json = objectMapper.writeValueAsString(payload);

            mockMvc.perform(post("/api/snippets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().is2xxSuccessful());

            List<Snippet> list = snippetRepository.findAll();
            Snippet saved = list.stream().filter(sn -> "No Status".equals(sn.getTitle())).findFirst().orElse(null);
            assertNotNull(saved);
            assertEquals("PENDING", saved.getStatus());
        }
    }

    // =========================================================================
    // TIER 3: CROSS-FEATURE INTERACTIONS
    // =========================================================================
    @Nested
    @DisplayName("Tier 3: Cross-Feature Interactions")
    class Tier3CrossFeatureInteractionsTests {

        @Test
        @DisplayName("F31+F30 [Tier 3]: POST new snippet, then GET by ID verifies complete fidelity")
        void testPostRestThenGetRestFidelity() throws Exception {
            Snippet payload = buildSnippet("NNM24CS076", "Dhanush U. Shetty", "dhanush@nmamit.in", 4,
                    "Java", "RoundTrip Test", "int sum = a + b;");
            String json = objectMapper.writeValueAsString(payload);

            mockMvc.perform(post("/api/snippets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().is2xxSuccessful());

            Snippet saved = snippetRepository.findAll().stream()
                    .filter(sn -> "RoundTrip Test".equals(sn.getTitle()))
                    .findFirst()
                    .orElseThrow();

            mockMvc.perform(get("/api/snippets/" + saved.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(saved.getId()))
                    .andExpect(jsonPath("$.usn").value("NNM24CS076"))
                    .andExpect(jsonPath("$.studentName").value("Dhanush U. Shetty"))
                    .andExpect(jsonPath("$.email").value("dhanush@nmamit.in"))
                    .andExpect(jsonPath("$.labExpNo").value(4))
                    .andExpect(jsonPath("$.language").value("Java"))
                    .andExpect(jsonPath("$.title").value("RoundTrip Test"))
                    .andExpect(jsonPath("$.rawCode").value("int sum = a + b;"))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        @DisplayName("F14+F30 [Tier 3]: Snippet created via SnippetManager is immediately fetchable via REST GET")
        void testServiceCreationThenRestGet() throws Exception {
            Snippet s = persistSnippet("NNM24CS107", "Jeston Correa", "jeston@nmamit.in", 2, "Java", "Service Created", "void run() {}");

            mockMvc.perform(get("/api/snippets/" + s.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Service Created"))
                    .andExpect(jsonPath("$.usn").value("NNM24CS107"));
        }

        @Test
        @DisplayName("F32+F14 [Tier 3]: Snippet updated via REST PUT is reflected when querying SnippetManager")
        void testRestPutThenServiceDirectVerification() throws Exception {
            Snippet s = persistSnippet("NNM24CS108", "Jnanesh Karkera", "jnanesh@nmamit.in", 3, "SQL", "Before Update", "SELECT *");

            s.setTitle("After Update");
            s.setStatus("APPROVED");
            s.setMarks(8);
            s.setReviewerComments("Good work");
            String json = objectMapper.writeValueAsString(s);

            mockMvc.perform(put("/api/snippets/" + s.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk());

            Snippet inDb = snippetManager.getSnippetById(s.getId());
            assertEquals("APPROVED", inDb.getStatus());
            assertEquals(8, inDb.getMarks());
            assertEquals("Good work", inDb.getReviewerComments());
        }

        @Test
        @DisplayName("F33+F14 [Tier 3]: Snippet deleted via REST DELETE reflects exists=false in SnippetManager")
        void testRestDeleteThenServiceExistsCheck() throws Exception {
            Snippet s = persistSnippet("NNM24CS100", "Ian Roniel Soans", "ian@nmamit.in", 5, "HTML", "To Delete", "<h1>hi</h1>");

            assertTrue(snippetManager.exists(s.getId()));

            mockMvc.perform(delete("/api/snippets/" + s.getId()))
                    .andExpect(status().isOk());

            assertFalse(snippetManager.exists(s.getId()));
        }
    }

    // =========================================================================
    // TIER 4: REAL-WORLD REST WORKLOAD SCENARIOS
    // =========================================================================
    @Nested
    @DisplayName("Tier 4: Real-World REST Lifecycle Scenarios")
    class Tier4RealWorldWorkloadScenarios {

        @Test
        @DisplayName("Scenario: Full CRUD lifecycle via REST (Create, Read, Update, Delete, 404 verify)")
        void testFullRestLifecycleWorkloadScenario() throws Exception {
            // 1. Initial list empty
            mockMvc.perform(get("/api/snippets"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));

            // 2. Student submits lab code with HTML tags
            Snippet newSnippet = buildSnippet("NNM24CS107", "Jeston Correa", "jeston@nmamit.in", 1,
                    "Java", "Servlet Lifecycle", "<servlet><servlet-name>demo</servlet-name></servlet>");
            mockMvc.perform(post("/api/snippets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newSnippet)))
                    .andExpect(status().is2xxSuccessful());

            // 3. Verify snippet appears in list and retrieve single snippet
            Snippet created = snippetRepository.findAll().get(0);
            mockMvc.perform(get("/api/snippets/" + created.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sanitizedCode").value(containsString("&lt;servlet&gt;")))
                    .andExpect(jsonPath("$.status").value("PENDING"));

            // 4. Reviewer grades snippet with marks and feedback via PUT
            created.setStatus("APPROVED");
            created.setMarks(10);
            created.setReviewerComments("Flawless deployment descriptor mapping.");
            mockMvc.perform(put("/api/snippets/" + created.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(created)))
                    .andExpect(status().isOk());

            // 5. Verify updated grading
            mockMvc.perform(get("/api/snippets/" + created.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("APPROVED"))
                    .andExpect(jsonPath("$.marks").value(10))
                    .andExpect(jsonPath("$.reviewerComments").value("Flawless deployment descriptor mapping."));

            // 6. Delete snippet
            mockMvc.perform(delete("/api/snippets/" + created.getId()))
                    .andExpect(status().isOk());

            // 7. Verify 404 NOT FOUND after deletion
            mockMvc.perform(get("/api/snippets/" + created.getId()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Scenario: 4 students submit distinct lab snippets via REST simultaneously")
        void testMultiStudentConcurrentRestSubmissionsScenario() throws Exception {
            Snippet s1 = buildSnippet("NNM24CS107", "Jeston Correa", "jeston@nmamit.in", 1, "Java", "Exp 1 Java", "System.out.println(1);");
            Snippet s2 = buildSnippet("NNM24CS108", "Jnanesh Karkera", "jnanesh@nmamit.in", 2, "JSP", "Exp 2 JSP", "<%= 2 %>");
            Snippet s3 = buildSnippet("NNM24CS100", "Ian Roniel Soans", "ian@nmamit.in", 3, "SQL", "Exp 3 SQL", "SELECT 3;");
            Snippet s4 = buildSnippet("NNM24CS076", "Dhanush U. Shetty", "dhanush@nmamit.in", 4, "HTML", "Exp 4 HTML", "<h1>4</h1>");

            mockMvc.perform(post("/api/snippets").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(s1))).andExpect(status().is2xxSuccessful());
            mockMvc.perform(post("/api/snippets").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(s2))).andExpect(status().is2xxSuccessful());
            mockMvc.perform(post("/api/snippets").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(s3))).andExpect(status().is2xxSuccessful());
            mockMvc.perform(post("/api/snippets").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(s4))).andExpect(status().is2xxSuccessful());

            MvcResult listResult = mockMvc.perform(get("/api/snippets"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(4))
                    .andReturn();

            String jsonOutput = listResult.getResponse().getContentAsString();
            assertTrue(jsonOutput.contains("NNM24CS107"));
            assertTrue(jsonOutput.contains("NNM24CS108"));
            assertTrue(jsonOutput.contains("NNM24CS100"));
            assertTrue(jsonOutput.contains("NNM24CS076"));
        }
    }
}
