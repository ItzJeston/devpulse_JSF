package com.example.demo;

import com.example.demo.entity.Snippet;
import com.example.demo.repository.SnippetRepository;
import com.example.demo.service.SnippetManager;
import com.example.demo.servlet.SessionServlet;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-End multi-component integration workflow test suite.
 * Covers:
 * - Tier 3: 5 Cross-Feature Pairwise Interactions between Servlets, Web MVC, JPA, and REST.
 * - Tier 4: 5 Real-World Academic Lab Submission, Review, and Defense Workflows.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class DevPulseE2EWorkflowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SnippetManager snippetManager;

    @Autowired
    private SnippetRepository snippetRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        snippetRepository.deleteAll();
    }

    /**
     * Executes real authentication through SessionServlet doPost lifecycle,
     * returning the resulting authenticated MockHttpSession.
     */
    private MockHttpSession authenticateViaSessionServlet(String username, String password) throws Exception {
        SessionServlet servlet = new SessionServlet();
        servlet.init();

        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        req.setParameter("username", username);
        req.setParameter("password", password);

        servlet.doPost(req, res);

        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, res.getStatus(), "Login must redirect on completion");
        assertTrue(res.getRedirectedUrl().contains("/dashboard"), "Successful login must redirect to /dashboard");

        MockHttpSession session = (MockHttpSession) req.getSession(false);
        assertNotNull(session, "HttpSession must be created on valid login");
        assertEquals(username, session.getAttribute("username"));
        return session;
    }

    private Snippet createAndSaveSnippet(String usn, String studentName, String email,
                                         int expNo, String language, String title,
                                         String rawCode, String sanitizedCode,
                                         String status, int marks, String reviewerComments) {
        Snippet s = new Snippet();
        s.setUsn(usn);
        s.setStudentName(studentName);
        s.setEmail(email);
        s.setLabExpNo(expNo);
        s.setLanguage(language);
        s.setTitle(title);
        s.setRawCode(rawCode);
        s.setSanitizedCode(sanitizedCode);
        s.setStatus(status);
        s.setMarks(marks);
        s.setReviewerComments(reviewerComments);
        snippetManager.saveSnippet(s);
        return s;
    }

    // =========================================================================
    // TIER 3: CROSS-FEATURE PAIRWISE INTERACTIONS
    // =========================================================================
    @Nested
    @DisplayName("Tier 3: Cross-Feature Pairwise Interactions")
    class Tier3PairwiseInteractions {

        @Test
        @DisplayName("Interaction 1: Student Login -> Submit XSS Code -> Preview Sanitized -> Persist -> Verify on Dashboard")
        void testPairwise_StudentLogin_SubmitXssCode_Persist_VerifyInDashboard() throws Exception {
            // 1. Authenticate student via SessionServlet
            MockHttpSession session = authenticateViaSessionServlet("NNM24CS107", "pass107");
            assertEquals("STUDENT", session.getAttribute("role"));
            assertEquals("Jeston Correa", session.getAttribute("fullName"));

            // 2. Submit form for validation and preview
            String rawCode = "public class Exp1 { public static void main(String[] args) { <script>alert('xss');</script> } }";
            mockMvc.perform(post("/submit")
                            .session(session)
                            .param("usn", "NNM24CS107")
                            .param("studentName", "Jeston Correa")
                            .param("email", "jeston@nmamit.in")
                            .param("labExpNo", "1")
                            .param("language", "Java")
                            .param("title", "Exp 1 Servlets")
                            .param("rawCode", rawCode))
                    .andExpect(status().isOk())
                    .andExpect(view().name("submit_result"))
                    .andExpect(model().attribute("isValid", true))
                    .andExpect(model().attribute("sanitizedCode", containsString("&lt;script&gt;alert(&#x27;xss&#x27;);&lt;/script&gt;")));

            // 3. Confirm and persist snippet
            String sanitized = rawCode.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");

            mockMvc.perform(post("/saveValidated")
                            .session(session)
                            .param("usn", "NNM24CS107")
                            .param("studentName", "Jeston Correa")
                            .param("email", "jeston@nmamit.in")
                            .param("labExpNo", "1")
                            .param("language", "Java")
                            .param("title", "Exp 1 Servlets")
                            .param("rawCode", rawCode)
                            .param("sanitizedCode", sanitized))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));

            // 4. Verify in DB
            List<Snippet> list = snippetRepository.findAll();
            assertEquals(1, list.size());
            Snippet saved = list.get(0);
            assertEquals("PENDING", saved.getStatus());
            assertTrue(saved.getSanitizedCode().contains("&lt;script&gt;"));

            // 5. Verify on Dashboard
            mockMvc.perform(get("/dashboard").session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("dashboard"))
                    .andExpect(model().attribute("snippetlist", hasSize(1)));
        }

        @Test
        @DisplayName("Interaction 2: Reviewer Login -> Grade Snippet via Web MVC -> Verify Update via REST API")
        void testPairwise_ReviewerLogin_GradeSnippet_VerifyViaRestApi() throws Exception {
            // 1. Seed unreviewed snippet in database
            Snippet s = createAndSaveSnippet("NNM24CS108", "Jnanesh Karkera", "jnanesh@nmamit.in",
                    2, "JSP", "JSP Basics", "int a = 1;", "int a = 1;", "PENDING", 0, "");

            // 2. Authenticate reviewer via SessionServlet
            MockHttpSession reviewerSession = authenticateViaSessionServlet("admin", "password");
            assertEquals("REVIEWER", reviewerSession.getAttribute("role"));

            // 3. Open review form
            mockMvc.perform(get("/review/" + s.getId()).session(reviewerSession))
                    .andExpect(status().isOk())
                    .andExpect(view().name("review_form"))
                    .andExpect(model().attributeExists("snippet"));

            // 4. Submit grading & comments
            mockMvc.perform(post("/saveReview")
                            .session(reviewerSession)
                            .param("id", s.getId().toString())
                            .param("status", "APPROVED")
                            .param("marks", "9")
                            .param("reviewerComments", "Excellent adherence to Java naming conventions."))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));

            // 5. Verify DB state via SnippetManager
            Snippet updated = snippetManager.getSnippetById(s.getId());
            assertEquals("APPROVED", updated.getStatus());
            assertEquals(9, updated.getMarks());
            assertEquals("Excellent adherence to Java naming conventions.", updated.getReviewerComments());

            // 6. Inspect via REST API
            mockMvc.perform(get("/api/snippets/" + s.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value("APPROVED"))
                    .andExpect(jsonPath("$.marks").value(9))
                    .andExpect(jsonPath("$.reviewerComments").value("Excellent adherence to Java naming conventions."));
        }

        @Test
        @DisplayName("Interaction 3: Student A creates -> Student B deletion blocked -> Reviewer deletion succeeds")
        void testPairwise_CrossStudentDeleteBlocked_ReviewerDeleteSucceeds() throws Exception {
            // 1. Student 1 logs in and saves a snippet
            MockHttpSession student1Session = authenticateViaSessionServlet("NNM24CS107", "pass107");
            Snippet s = createAndSaveSnippet("NNM24CS107", "Jeston Correa", "jeston@nmamit.in",
                    1, "Java", "Student 1 Lab", "code", "code", "PENDING", 0, "");

            // 2. Student 2 logs in with distinct session
            MockHttpSession student2Session = authenticateViaSessionServlet("NNM24CS108", "pass108");

            // 3. Student 2 attempts unauthorized deletion of Student 1's snippet
            mockMvc.perform(get("/delete/" + s.getId()).session(student2Session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));

            assertTrue(snippetManager.exists(s.getId()), "Snippet must still exist after unauthorized delete attempt");

            // 4. Reviewer logs in and executes deletion
            MockHttpSession reviewerSession = authenticateViaSessionServlet("admin", "password");
            mockMvc.perform(get("/delete/" + s.getId()).session(reviewerSession))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));

            assertFalse(snippetManager.exists(s.getId()), "Snippet must be deleted after reviewer delete execution");
        }

        @Test
        @DisplayName("Interaction 4: POST via REST API -> View on Web Dashboard -> Grade via Web MVC -> Fetch via REST")
        void testPairwise_RestPostSnippet_MvcDashboardAndGrade_RestGetVerification() throws Exception {
            // 1. External REST client creates snippet
            Snippet restPayload = new Snippet();
            restPayload.setUsn("NNM24CS100");
            restPayload.setStudentName("Ian Roniel Soans");
            restPayload.setEmail("ian@nmamit.in");
            restPayload.setLabExpNo(3);
            restPayload.setLanguage("SQL");
            restPayload.setTitle("JDBC Statements");
            restPayload.setRawCode("SELECT * FROM users WHERE 1=1;");
            restPayload.setStatus("PENDING");

            mockMvc.perform(post("/api/snippets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(restPayload)))
                    .andExpect(status().is2xxSuccessful());

            Snippet created = snippetRepository.findAll().stream()
                    .filter(sn -> "JDBC Statements".equals(sn.getTitle()))
                    .findFirst()
                    .orElseThrow();

            // 2. Reviewer logs in to Web MVC and inspects dashboard
            MockHttpSession reviewerSession = authenticateViaSessionServlet("admin", "password");
            mockMvc.perform(get("/dashboard").session(reviewerSession))
                    .andExpect(status().isOk())
                    .andExpect(view().name("dashboard"))
                    .andExpect(model().attribute("snippetlist", hasItem(hasProperty("title", equalTo("JDBC Statements")))));

            // 3. Reviewer submits grading via Web MVC
            mockMvc.perform(post("/saveReview")
                            .session(reviewerSession)
                            .param("id", created.getId().toString())
                            .param("status", "APPROVED")
                            .param("marks", "10")
                            .param("reviewerComments", "Clean SQL syntax"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));

            // 4. External REST client fetches updated snippet
            mockMvc.perform(get("/api/snippets/" + created.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("APPROVED"))
                    .andExpect(jsonPath("$.marks").value(10))
                    .andExpect(jsonPath("$.reviewerComments").value("Clean SQL syntax"));
        }

        @Test
        @DisplayName("Interaction 5: Session Invalidation (Logout) locks out protected /new endpoint")
        void testPairwise_SessionLogout_LocksOutProtectedNewSnippetEndpoint() throws Exception {
            // 1. Student logs in
            MockHttpSession session = authenticateViaSessionServlet("NNM24CS107", "pass107");

            // 2. Verify access to protected /new
            mockMvc.perform(get("/new").session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("submit_snippet"));

            // 3. Logout via SessionServlet
            SessionServlet servlet = new SessionServlet();
            servlet.init();

            MockHttpServletRequest logoutReq = new MockHttpServletRequest();
            MockHttpServletResponse logoutRes = new MockHttpServletResponse();
            logoutReq.setSession(session);
            logoutReq.setParameter("action", "logout");

            servlet.doGet(logoutReq, logoutRes);

            assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, logoutRes.getStatus());
            assertTrue(logoutRes.getRedirectedUrl().contains("/login"));
            assertTrue(session.isInvalid(), "Session must be invalidated on logout");

            // 4. Subsequent access to /new with invalidated session is blocked
            mockMvc.perform(get("/new").session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }
    }

    // =========================================================================
    // TIER 4: REAL-WORLD APPLICATION SCENARIOS
    // =========================================================================
    @Nested
    @DisplayName("Tier 4: Real-World Application Scenarios")
    class Tier4RealWorldWorkflows {

        @Test
        @DisplayName("Scenario 1: Student Full Lab Cycle (Login -> Prepare -> Preview -> Persist -> Dashboard -> Diagnostics -> Logout)")
        void testScenario1_StudentFullLabCycle() throws Exception {
            // Step 1: Student Login
            MockHttpSession session = authenticateViaSessionServlet("NNM24CS107", "pass107");
            assertEquals("NNM24CS107", session.getAttribute("username"));
            assertEquals("STUDENT", session.getAttribute("role"));
            assertEquals("Jeston Correa", session.getAttribute("fullName"));

            // Step 2: Open Submission Form
            mockMvc.perform(get("/new").session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("submit_snippet"));

            // Step 3: Submit Form for Validation & Preview
            String rawCode = "public class LifeCycleServlet extends HttpServlet { <script>alert('run');</script> }";
            mockMvc.perform(post("/submit")
                            .session(session)
                            .param("usn", "NNM24CS107")
                            .param("studentName", "Jeston Correa")
                            .param("email", "jeston@nmamit.in")
                            .param("labExpNo", "1")
                            .param("language", "Java")
                            .param("title", "Servlet Lifecycle & Config")
                            .param("rawCode", rawCode))
                    .andExpect(status().isOk())
                    .andExpect(view().name("submit_result"))
                    .andExpect(model().attribute("isValid", true))
                    .andExpect(model().attribute("sanitizedCode", containsString("&lt;script&gt;alert(&#x27;run&#x27;);&lt;/script&gt;")));

            // Step 4: Confirm & Save Snippet
            String sanitized = rawCode.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");

            mockMvc.perform(post("/saveValidated")
                            .session(session)
                            .param("usn", "NNM24CS107")
                            .param("studentName", "Jeston Correa")
                            .param("email", "jeston@nmamit.in")
                            .param("labExpNo", "1")
                            .param("language", "Java")
                            .param("title", "Servlet Lifecycle & Config")
                            .param("rawCode", rawCode)
                            .param("sanitizedCode", sanitized))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));

            assertEquals(1, snippetRepository.count());

            // Step 5: Verify on Dashboard
            mockMvc.perform(get("/dashboard").session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("dashboard"))
                    .andExpect(model().attribute("snippetlist", hasSize(1)));

            // Step 6: Inspect Session Diagnostics
            SessionServlet servlet = new SessionServlet();
            servlet.init();

            MockHttpServletRequest infoReq = new MockHttpServletRequest();
            MockHttpServletResponse infoRes = new MockHttpServletResponse();
            infoReq.setSession(session);
            infoReq.setParameter("action", "info");

            servlet.doGet(infoReq, infoRes);
            assertEquals(200, infoRes.getStatus());
            assertTrue(infoRes.getContentAsString().contains(session.getId()));
            assertTrue(infoRes.getContentAsString().contains("NNM24CS107"));
            assertTrue(infoRes.getContentAsString().contains("STUDENT"));

            // Step 7: Logout & Invalidation
            MockHttpServletRequest logoutReq = new MockHttpServletRequest();
            MockHttpServletResponse logoutRes = new MockHttpServletResponse();
            logoutReq.setSession(session);
            logoutReq.setParameter("action", "logout");

            servlet.doGet(logoutReq, logoutRes);
            assertEquals(302, logoutRes.getStatus());
            assertTrue(logoutRes.getRedirectedUrl().contains("/login"));
            assertTrue(session.isInvalid());
        }

        @Test
        @DisplayName("Scenario 2: Faculty Reviewer Evaluation (Inspect Queue -> Review Form -> Grade & Comment -> REST Verify)")
        void testScenario2_FacultyReviewerEvaluationWorkflow() throws Exception {
            // Step 1: Pre-condition - Seed student submission
            Snippet s = createAndSaveSnippet("NNM24CS076", "Dhanush U. Shetty", "dhanush@nmamit.in",
                    4, "Java", "JPA Entity Mapping",
                    "@Entity public class Book {}", "@Entity public class Book {}",
                    "PENDING", 0, "");

            // Step 2: Reviewer Login
            MockHttpSession reviewerSession = authenticateViaSessionServlet("admin", "password");
            assertEquals("REVIEWER", reviewerSession.getAttribute("role"));

            // Step 3: Inspect Dashboard Queue
            mockMvc.perform(get("/dashboard").session(reviewerSession))
                    .andExpect(status().isOk())
                    .andExpect(view().name("dashboard"))
                    .andExpect(model().attribute("snippetlist", hasItem(hasProperty("title", equalTo("JPA Entity Mapping")))));

            // Step 4: Open Review Form
            mockMvc.perform(get("/review/" + s.getId()).session(reviewerSession))
                    .andExpect(status().isOk())
                    .andExpect(view().name("review_form"))
                    .andExpect(model().attribute("snippet", hasProperty("id", equalTo(s.getId()))));

            // Step 5: Submit Grading & Comments
            mockMvc.perform(post("/saveReview")
                            .session(reviewerSession)
                            .param("id", s.getId().toString())
                            .param("status", "APPROVED")
                            .param("marks", "9")
                            .param("reviewerComments", "Excellent JPA entity mapping with correct identity strategy."))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));

            // Step 6: Verify Database Entity State
            Snippet updated = snippetManager.getSnippetById(s.getId());
            assertEquals("APPROVED", updated.getStatus());
            assertEquals(9, updated.getMarks());
            assertTrue(updated.getReviewerComments().contains("Excellent JPA entity mapping"));

            // Step 7: Verify REST API Output
            mockMvc.perform(get("/api/snippets/" + s.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("APPROVED"))
                    .andExpect(jsonPath("$.marks").value(9))
                    .andExpect(jsonPath("$.reviewerComments").value("Excellent JPA entity mapping with correct identity strategy."));
        }

        @Test
        @DisplayName("Scenario 3: Multi-Student Concurrent Submissions across Java, JSP, and SQL")
        void testScenario3_MultiStudentConcurrentSubmissions() throws Exception {
            // Four student submission vectors
            String[][] submissions = {
                    {"NNM24CS107", "pass107", "Jeston Correa", "jeston@nmamit.in", "1", "Java", "Exp 1 Servlets", "System.out.println(1);"},
                    {"NNM24CS108", "pass108", "Jnanesh Karkera", "jnanesh@nmamit.in", "2", "JSP", "Exp 2 Form Validation", "<%= 2 %>"},
                    {"NNM24CS100", "pass100", "Ian Roniel Soans", "ian@nmamit.in", "3", "SQL", "Exp 3 Connection", "SELECT 3;"},
                    {"NNM24CS076", "pass076", "Dhanush U. Shetty", "dhanush@nmamit.in", "4", "Java", "Exp 4 Spring Data JPA", "class Book {}"}
            };

            for (String[] sub : submissions) {
                MockHttpSession studentSession = authenticateViaSessionServlet(sub[0], sub[1]);

                mockMvc.perform(post("/submit")
                                .session(studentSession)
                                .param("usn", sub[0])
                                .param("studentName", sub[2])
                                .param("email", sub[3])
                                .param("labExpNo", sub[4])
                                .param("language", sub[5])
                                .param("title", sub[6])
                                .param("rawCode", sub[7]))
                        .andExpect(status().isOk())
                        .andExpect(model().attribute("isValid", true));

                mockMvc.perform(post("/saveValidated")
                                .session(studentSession)
                                .param("usn", sub[0])
                                .param("studentName", sub[2])
                                .param("email", sub[3])
                                .param("labExpNo", sub[4])
                                .param("language", sub[5])
                                .param("title", sub[6])
                                .param("rawCode", sub[7])
                                .param("sanitizedCode", sub[7]))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(redirectedUrl("/dashboard"));
            }

            // Verify Database Population & Primary Key Uniqueness
            List<Snippet> all = snippetRepository.findAll();
            assertEquals(4, all.size(), "All 4 student snippets must be persisted");

            Set<Long> ids = all.stream().map(Snippet::getId).collect(Collectors.toSet());
            assertEquals(4, ids.size(), "Zero ID collision across submissions");

            // Reviewer logs in and inspects dashboard
            MockHttpSession reviewerSession = authenticateViaSessionServlet("admin", "password");
            mockMvc.perform(get("/dashboard").session(reviewerSession))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("snippetlist", hasSize(4)));

            // Verify REST API Completeness
            mockMvc.perform(get("/api/snippets"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(4))
                    .andExpect(jsonPath("$[?(@.usn == 'NNM24CS107')].language").value("Java"))
                    .andExpect(jsonPath("$[?(@.usn == 'NNM24CS108')].language").value("JSP"))
                    .andExpect(jsonPath("$[?(@.usn == 'NNM24CS100')].language").value("SQL"))
                    .andExpect(jsonPath("$[?(@.usn == 'NNM24CS076')].language").value("Java"));
        }

        @Test
        @DisplayName("Scenario 4: External Evaluator REST Assessment (Full CRUD lifecycle via REST)")
        void testScenario4_ExternalEvaluatorRestAssessment() throws Exception {
            // Step 1: POST New Snippet with unsanitized code
            String payload = "{\"usn\":\"NNM24CS107\",\"studentName\":\"Jeston Correa\",\"email\":\"jeston@nmamit.in\"," +
                    "\"labExpNo\":5,\"language\":\"Java\",\"title\":\"REST Benchmark\"," +
                    "\"rawCode\":\"@GetMapping(\\\"/test\\\") public String test() { return \\\"<script>alert('rest');</script>\\\"; }\"}";

            mockMvc.perform(post("/api/snippets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().is2xxSuccessful());

            // Step 2: Extract ID from database
            Long snippetId = snippetRepository.findAll().get(0).getId();
            assertNotNull(snippetId);

            // Step 3: GET by ID & Verify Auto-Sanitization
            mockMvc.perform(get("/api/snippets/" + snippetId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(snippetId))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.sanitizedCode").value(containsString("&lt;script&gt;alert(&#x27;rest&#x27;);&lt;/script&gt;")));

            // Step 4: PUT Update Snippet
            String updatePayload = "{\"id\":" + snippetId + ",\"usn\":\"NNM24CS107\",\"studentName\":\"Jeston Correa\"," +
                    "\"email\":\"jeston@nmamit.in\",\"labExpNo\":5,\"language\":\"Java\",\"title\":\"REST Benchmark\"," +
                    "\"rawCode\":\"updated\",\"sanitizedCode\":\"updated\",\"status\":\"APPROVED\"," +
                    "\"reviewerComments\":\"Verified via REST automated grader\",\"marks\":10}";

            mockMvc.perform(put("/api/snippets/" + snippetId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updatePayload))
                    .andExpect(status().isOk());

            // Step 5: GET by ID & Verify Persistence of Update
            mockMvc.perform(get("/api/snippets/" + snippetId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("APPROVED"))
                    .andExpect(jsonPath("$.marks").value(10))
                    .andExpect(jsonPath("$.reviewerComments").value("Verified via REST automated grader"));

            // Step 6: DELETE Snippet
            mockMvc.perform(delete("/api/snippets/" + snippetId))
                    .andExpect(status().isOk());

            // Step 7: Verify 404 NOT FOUND after Deletion
            mockMvc.perform(get("/api/snippets/" + snippetId))
                    .andExpect(status().isNotFound());

            // Step 8: Verify Idempotent/Secondary DELETE returns 404
            mockMvc.perform(delete("/api/snippets/" + snippetId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Scenario 5: Comprehensive Security and Injection Defense Scenario")
        void testScenario5_SecurityAndInjectionDefense() throws Exception {
            // Defense Vector A: SQL Injection Defense in Login
            SessionServlet servlet = new SessionServlet();
            servlet.init();

            MockHttpServletRequest loginReq = new MockHttpServletRequest();
            MockHttpServletResponse loginRes = new MockHttpServletResponse();
            loginReq.setParameter("username", "' OR '1'='1' -- ");
            loginReq.setParameter("password", "' OR '1'='1'");

            servlet.doPost(loginReq, loginRes);
            assertEquals(302, loginRes.getStatus());
            assertTrue(loginRes.getRedirectedUrl().contains("/login?error=true"));

            HttpSession sqliSession = loginReq.getSession(false);
            assertTrue(sqliSession == null || sqliSession.getAttribute("username") == null,
                    "SQL injection in login must never create an authenticated session");

            // Defense Vector B: Stored XSS Sanitization in Code Submission
            MockHttpSession studentSession = authenticateViaSessionServlet("NNM24CS107", "pass107");

            String maliciousCode = "<script src=\"http://malicious.example.com/steal.js\"></script>" +
                    "<img src=\"x\" onerror=\"alert(document.cookie)\"/>" +
                    "<iframe src=\"javascript:alert(1)\"></iframe>";

            mockMvc.perform(post("/submit")
                            .session(studentSession)
                            .param("usn", "NNM24CS107")
                            .param("studentName", "Jeston Correa")
                            .param("email", "jeston@nmamit.in")
                            .param("labExpNo", "1")
                            .param("language", "Java")
                            .param("title", "XSS Attack Lab")
                            .param("rawCode", maliciousCode))
                    .andExpect(status().isOk())
                    .andExpect(view().name("submit_result"))
                    .andExpect(model().attribute("isValid", true))
                    .andExpect(model().attribute("sanitizedCode", containsString("&lt;script src=&quot;http://malicious.example.com/steal.js&quot;&gt;&lt;/script&gt;")))
                    .andExpect(model().attribute("sanitizedCode", containsString("&lt;img src=&quot;x&quot; onerror=&quot;alert(document.cookie)&quot;/&gt;")))
                    .andExpect(model().attribute("sanitizedCode", containsString("&lt;iframe src=&quot;javascript:alert(1)&quot;&gt;&lt;/iframe&gt;")));

            // Defense Vector C: Student Privilege Escalation Defense
            Snippet victimSnippet = createAndSaveSnippet("NNM24CS108", "Jnanesh Karkera", "jnanesh@nmamit.in",
                    2, "JSP", "Lab 2", "code", "code", "PENDING", 0, "");

            mockMvc.perform(get("/review/" + victimSnippet.getId()).session(studentSession))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));

            mockMvc.perform(post("/saveReview")
                            .session(studentSession)
                            .param("id", victimSnippet.getId().toString())
                            .param("marks", "10")
                            .param("status", "APPROVED"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));

            Snippet unhacked = snippetManager.getSnippetById(victimSnippet.getId());
            assertEquals("PENDING", unhacked.getStatus());
            assertEquals(0, unhacked.getMarks());

            // Defense Vector D: Cross-Student Snippet Tampering Defense
            mockMvc.perform(get("/delete/" + victimSnippet.getId()).session(studentSession))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));

            assertTrue(snippetManager.exists(victimSnippet.getId()), "Cross-student deletion must be blocked");

            // Defense Vector E: Parameter Tampering Defense
            mockMvc.perform(post("/submit")
                            .session(studentSession)
                            .param("usn", "NNM24CS107")
                            .param("studentName", "Jeston Correa")
                            .param("email", "jeston@nmamit.in")
                            .param("labExpNo", "-9999")
                            .param("language", "SQL; DROP TABLE snippet; --")
                            .param("title", "Tampered Form")
                            .param("rawCode", "test"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("submit_result"))
                    .andExpect(model().attribute("isValid", false))
                    .andExpect(model().attributeExists("errors"));

            // Database remains fully operational
            assertEquals(1, snippetRepository.count());
        }
    }
}
