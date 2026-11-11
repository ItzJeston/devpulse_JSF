package com.example.demo.controller;

import com.example.demo.entity.Snippet;
import com.example.demo.repository.SnippetRepository;
import com.example.demo.service.SnippetManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test suite for WebController (Experiment 4 Spring MVC Layer).
 * Exercises public routing, session-based authentication checks, submission preview,
 * validation error propagation, persistence, reviewer grading, and deletion authorization.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class WebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SnippetManager snippetManager;

    @Autowired
    private SnippetRepository snippetRepository;

    @BeforeEach
    void setUp() {
        snippetRepository.deleteAll();
    }

    private MockHttpSession createStudentSession(String username, String fullName) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", username);
        session.setAttribute("role", "STUDENT");
        session.setAttribute("fullName", fullName);
        return session;
    }

    private MockHttpSession createReviewerSession(String username, String fullName) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", username);
        session.setAttribute("role", "REVIEWER");
        session.setAttribute("fullName", fullName);
        return session;
    }

    private Snippet persistSnippet(String usn, String name, String email, int expNo,
                                   String language, String title, String rawCode, String sanitizedCode) {
        Snippet s = new Snippet();
        s.setUsn(usn);
        s.setStudentName(name);
        s.setEmail(email);
        s.setLabExpNo(expNo);
        s.setLanguage(language);
        s.setTitle(title);
        s.setRawCode(rawCode);
        s.setSanitizedCode(sanitizedCode);
        s.setStatus("PENDING");
        s.setReviewerComments("");
        s.setMarks(0);
        snippetManager.saveSnippet(s);
        return s;
    }

    // =========================================================================
    // 1. PUBLIC & LOGIN NAVIGATION (Feature 21)
    // =========================================================================
    @Nested
    @DisplayName("Public and Login Navigation")
    class PublicAndLoginNavigationTests {

        @Test
        @DisplayName("F21: Root URL navigates to login view")
        void testRootUrlNavigatesToLoginView() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("login"));
        }

        @Test
        @DisplayName("F21: /login URL navigates to login view")
        void testLoginUrlNavigatesToLoginView() throws Exception {
            mockMvc.perform(get("/login"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("login"));
        }

        @Test
        @DisplayName("F21: /login?error=true renders login view with error indicator")
        void testLoginUrlWithErrorParameterDisplaysError() throws Exception {
            mockMvc.perform(get("/login").param("error", "true"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("login"));
        }

        @Test
        @DisplayName("F21: /login?logout=true renders login view with logout indicator")
        void testLoginUrlWithLogoutParameterDisplaysLogoutMessage() throws Exception {
            mockMvc.perform(get("/login").param("logout", "true"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("login"));
        }

        @Test
        @DisplayName("F21: Authenticated student navigating to /login redirects to /dashboard")
        void testAuthenticatedStudentNavigatingToLoginRedirectsToDashboard() throws Exception {
            MockHttpSession session = createStudentSession("NNM24CS107", "Jeston Correa");

            mockMvc.perform(get("/login").session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));
        }

        @Test
        @DisplayName("F21: Authenticated reviewer navigating to /login redirects to /dashboard")
        void testAuthenticatedReviewerNavigatingToLoginRedirectsToDashboard() throws Exception {
            MockHttpSession session = createReviewerSession("admin", "Prof. Akhilraj V. Gadagkar");

            mockMvc.perform(get("/login").session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));
        }
    }

    // =========================================================================
    // 2. DASHBOARD NAVIGATION & ROLE FILTERING (Feature 22)
    // =========================================================================
    @Nested
    @DisplayName("Dashboard Navigation and Session Gating")
    class DashboardNavigationTests {

        @Test
        @DisplayName("F22: Unauthenticated /dashboard access redirects to /login")
        void testDashboardUnauthenticatedRedirectsToLogin() throws Exception {
            mockMvc.perform(get("/dashboard"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }

        @Test
        @DisplayName("F22: Student session accesses /dashboard and receives snippetlist model")
        void testDashboardWithStudentSessionReturnsDashboardView() throws Exception {
            MockHttpSession session = createStudentSession("NNM24CS107", "Jeston Correa");

            mockMvc.perform(get("/dashboard").session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("dashboard"))
                    .andExpect(model().attributeExists("snippetlist"));
        }

        @Test
        @DisplayName("F22: Reviewer session accesses /dashboard and receives snippetlist model")
        void testDashboardWithReviewerSessionReturnsDashboardView() throws Exception {
            MockHttpSession session = createReviewerSession("admin", "Prof. Akhilraj V. Gadagkar");

            mockMvc.perform(get("/dashboard").session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("dashboard"))
                    .andExpect(model().attributeExists("snippetlist"));
        }

        @Test
        @DisplayName("F22: Dashboard renders persisted snippets accurately")
        void testDashboardDisplaysPersistedSnippets() throws Exception {
            persistSnippet("NNM24CS107", "Jeston Correa", "jeston@nmamit.in", 1, "Java", "Exp 1", "code1", "code1");
            persistSnippet("NNM24CS108", "Jnanesh Karkera", "jnanesh@nmamit.in", 2, "JSP", "Exp 2", "code2", "code2");

            MockHttpSession session = createStudentSession("NNM24CS107", "Jeston Correa");

            mockMvc.perform(get("/dashboard").session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("dashboard"))
                    .andExpect(model().attribute("snippetlist", hasSize(greaterThanOrEqualTo(2))));
        }

        @Test
        @DisplayName("F22: Empty repository renders empty snippetlist without exception")
        void testDashboardWithEmptyDatabaseRendersEmptyList() throws Exception {
            snippetRepository.deleteAll();
            MockHttpSession session = createReviewerSession("admin", "Prof. Akhilraj V. Gadagkar");

            mockMvc.perform(get("/dashboard").session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("dashboard"))
                    .andExpect(model().attribute("snippetlist", hasSize(0)));
        }

        @Test
        @DisplayName("F22: Session with empty username attribute redirects to /login")
        void testDashboardWithInvalidSessionAttributeRedirectsToLogin() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("username", "");

            mockMvc.perform(get("/dashboard").session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }
    }

    // =========================================================================
    // 3. STUDENT SUBMISSION FORM GATING (Feature 23)
    // =========================================================================
    @Nested
    @DisplayName("Student Submission Form Gating (/new)")
    class SubmissionFormGatingTests {

        @Test
        @DisplayName("F23: Unauthenticated /new redirects to /login")
        void testNewSnippetFormUnauthenticatedRedirectsToLogin() throws Exception {
            mockMvc.perform(get("/new"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }

        @Test
        @DisplayName("F23: Student role accesses /new successfully")
        void testNewSnippetFormWithStudentRoleReturnsSubmitSnippetView() throws Exception {
            MockHttpSession session = createStudentSession("NNM24CS107", "Jeston Correa");

            mockMvc.perform(get("/new").session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("submit_snippet"));
        }

        @Test
        @DisplayName("F23: Reviewer role accessing /new redirects to /dashboard")
        void testNewSnippetFormWithReviewerRoleRedirectsToDashboard() throws Exception {
            MockHttpSession session = createReviewerSession("admin", "Prof. Akhilraj V. Gadagkar");

            mockMvc.perform(get("/new").session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));
        }

        @Test
        @DisplayName("F23: Session details are accessible for /new rendering")
        void testNewSnippetFormPrePopulatesStudentSessionDetails() throws Exception {
            MockHttpSession session = createStudentSession("NNM24CS100", "Ian Roniel Soans");

            mockMvc.perform(get("/new").session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("submit_snippet"));
        }
    }

    // =========================================================================
    // 4. FORM SUBMISSION VALIDATION & PREVIEW (Feature 24)
    // =========================================================================
    @Nested
    @DisplayName("Form Submission Validation and Preview (POST /submit)")
    class FormValidationAndPreviewTests {

        @Test
        @DisplayName("F24: Valid form submission sets isValid=true and renders preview")
        void testSubmitValidSnippetReturnsSubmitResultPreview() throws Exception {
            MockHttpSession session = createStudentSession("NNM24CS107", "Jeston Correa");

            mockMvc.perform(post("/submit")
                            .session(session)
                            .param("usn", "NNM24CS107")
                            .param("studentName", "Jeston Correa")
                            .param("email", "jeston@nmamit.in")
                            .param("labExpNo", "1")
                            .param("language", "Java")
                            .param("title", "Servlet Basics")
                            .param("rawCode", "System.out.println(\"Hello\");"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("submit_result"))
                    .andExpect(model().attribute("isValid", true))
                    .andExpect(model().attributeExists("sanitizedCode"));
        }

        @Test
        @DisplayName("F24: Raw script tags are properly sanitized during form preview")
        void testSubmitValidSnippetSanitizesXssScriptTag() throws Exception {
            MockHttpSession session = createStudentSession("NNM24CS107", "Jeston Correa");

            mockMvc.perform(post("/submit")
                            .session(session)
                            .param("usn", "NNM24CS107")
                            .param("studentName", "Jeston Correa")
                            .param("email", "jeston@nmamit.in")
                            .param("labExpNo", "1")
                            .param("language", "Java")
                            .param("title", "XSS Test")
                            .param("rawCode", "<script>alert('xss');</script>"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("submit_result"))
                    .andExpect(model().attribute("isValid", true))
                    .andExpect(model().attribute("sanitizedCode", containsString("&lt;script&gt;alert(&#x27;xss&#x27;);&lt;/script&gt;")));
        }

        @Test
        @DisplayName("F24: Invalid USN format fails validation")
        void testSubmitSnippetWithInvalidUsnFormatFailsValidation() throws Exception {
            MockHttpSession session = createStudentSession("NNM24CS107", "Jeston Correa");

            mockMvc.perform(post("/submit")
                            .session(session)
                            .param("usn", "NNM24IS107")
                            .param("studentName", "Jeston Correa")
                            .param("email", "jeston@nmamit.in")
                            .param("labExpNo", "1")
                            .param("language", "Java")
                            .param("title", "Title")
                            .param("rawCode", "code"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("submit_result"))
                    .andExpect(model().attribute("isValid", false))
                    .andExpect(model().attributeExists("errors"));
        }

        @Test
        @DisplayName("F24: Invalid student name format fails validation")
        void testSubmitSnippetWithInvalidStudentNameFailsValidation() throws Exception {
            MockHttpSession session = createStudentSession("NNM24CS107", "Jeston Correa");

            mockMvc.perform(post("/submit")
                            .session(session)
                            .param("usn", "NNM24CS107")
                            .param("studentName", "Jeston_123")
                            .param("email", "jeston@nmamit.in")
                            .param("labExpNo", "1")
                            .param("language", "Java")
                            .param("title", "Title")
                            .param("rawCode", "code"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("submit_result"))
                    .andExpect(model().attribute("isValid", false))
                    .andExpect(model().attributeExists("errors"));
        }

        @Test
        @DisplayName("F24: Lab experiment number below range (<1) fails validation")
        void testSubmitSnippetWithLabExpNoOutOfRangeBelowFailsValidation() throws Exception {
            MockHttpSession session = createStudentSession("NNM24CS107", "Jeston Correa");

            mockMvc.perform(post("/submit")
                            .session(session)
                            .param("usn", "NNM24CS107")
                            .param("studentName", "Jeston Correa")
                            .param("email", "jeston@nmamit.in")
                            .param("labExpNo", "0")
                            .param("language", "Java")
                            .param("title", "Title")
                            .param("rawCode", "code"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("submit_result"))
                    .andExpect(model().attribute("isValid", false))
                    .andExpect(model().attributeExists("errors"));
        }

        @Test
        @DisplayName("F24: Lab experiment number above range (>10) fails validation")
        void testSubmitSnippetWithLabExpNoOutOfRangeAboveFailsValidation() throws Exception {
            MockHttpSession session = createStudentSession("NNM24CS107", "Jeston Correa");

            mockMvc.perform(post("/submit")
                            .session(session)
                            .param("usn", "NNM24CS107")
                            .param("studentName", "Jeston Correa")
                            .param("email", "jeston@nmamit.in")
                            .param("labExpNo", "11")
                            .param("language", "Java")
                            .param("title", "Title")
                            .param("rawCode", "code"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("submit_result"))
                    .andExpect(model().attribute("isValid", false))
                    .andExpect(model().attributeExists("errors"));
        }

        @Test
        @DisplayName("F24: Non-numeric lab experiment number fails validation")
        void testSubmitSnippetWithNonNumericLabExpNoFailsValidation() throws Exception {
            MockHttpSession session = createStudentSession("NNM24CS107", "Jeston Correa");

            mockMvc.perform(post("/submit")
                            .session(session)
                            .param("usn", "NNM24CS107")
                            .param("studentName", "Jeston Correa")
                            .param("email", "jeston@nmamit.in")
                            .param("labExpNo", "abc")
                            .param("language", "Java")
                            .param("title", "Title")
                            .param("rawCode", "code"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("submit_result"))
                    .andExpect(model().attribute("isValid", false))
                    .andExpect(model().attributeExists("errors"));
        }

        @Test
        @DisplayName("F24: Disallowed language fails validation")
        void testSubmitSnippetWithDisallowedLanguageFailsValidation() throws Exception {
            MockHttpSession session = createStudentSession("NNM24CS107", "Jeston Correa");

            mockMvc.perform(post("/submit")
                            .session(session)
                            .param("usn", "NNM24CS107")
                            .param("studentName", "Jeston Correa")
                            .param("email", "jeston@nmamit.in")
                            .param("labExpNo", "1")
                            .param("language", "Python")
                            .param("title", "Title")
                            .param("rawCode", "code"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("submit_result"))
                    .andExpect(model().attribute("isValid", false))
                    .andExpect(model().attributeExists("errors"));
        }

        @Test
        @DisplayName("F24: Malformed email fails validation")
        void testSubmitSnippetWithInvalidEmailFormatFailsValidation() throws Exception {
            MockHttpSession session = createStudentSession("NNM24CS107", "Jeston Correa");

            mockMvc.perform(post("/submit")
                            .session(session)
                            .param("usn", "NNM24CS107")
                            .param("studentName", "Jeston Correa")
                            .param("email", "jeston@nmamit")
                            .param("labExpNo", "1")
                            .param("language", "Java")
                            .param("title", "Title")
                            .param("rawCode", "code"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("submit_result"))
                    .andExpect(model().attribute("isValid", false))
                    .andExpect(model().attributeExists("errors"));
        }

        @Test
        @DisplayName("F24: Empty code input fails validation")
        void testSubmitSnippetWithEmptyCodeFailsValidation() throws Exception {
            MockHttpSession session = createStudentSession("NNM24CS107", "Jeston Correa");

            mockMvc.perform(post("/submit")
                            .session(session)
                            .param("usn", "NNM24CS107")
                            .param("studentName", "Jeston Correa")
                            .param("email", "jeston@nmamit.in")
                            .param("labExpNo", "1")
                            .param("language", "Java")
                            .param("title", "Title")
                            .param("rawCode", "   "))
                    .andExpect(status().isOk())
                    .andExpect(view().name("submit_result"))
                    .andExpect(model().attribute("isValid", false))
                    .andExpect(model().attributeExists("errors"));
        }

        @Test
        @DisplayName("F24: Multiple invalid fields collect all descriptive validation errors")
        void testSubmitSnippetWithMultipleInvalidFieldsCollectsAllErrors() throws Exception {
            MockHttpSession session = createStudentSession("NNM24CS107", "Jeston Correa");

            mockMvc.perform(post("/submit")
                            .session(session)
                            .param("usn", "INVALID_USN")
                            .param("studentName", "123_Invalid")
                            .param("email", "not-an-email")
                            .param("labExpNo", "99")
                            .param("language", "Ruby")
                            .param("title", "Test")
                            .param("rawCode", ""))
                    .andExpect(status().isOk())
                    .andExpect(view().name("submit_result"))
                    .andExpect(model().attribute("isValid", false))
                    .andExpect(model().attribute("errors", hasSize(greaterThanOrEqualTo(5))));
        }
    }

    // =========================================================================
    // 5. VALIDATED SNIPPET PERSISTENCE (Feature 25)
    // =========================================================================
    @Nested
    @DisplayName("Validated Snippet Persistence (POST /saveValidated)")
    class ValidatedSnippetPersistenceTests {

        @Test
        @DisplayName("F25: Unauthenticated /saveValidated redirects to /login")
        void testSaveValidatedUnauthenticatedRedirectsToLogin() throws Exception {
            mockMvc.perform(post("/saveValidated")
                            .param("usn", "NNM24CS107")
                            .param("studentName", "Jeston Correa")
                            .param("email", "jeston@nmamit.in")
                            .param("labExpNo", "1")
                            .param("language", "Java")
                            .param("title", "Title")
                            .param("rawCode", "code")
                            .param("sanitizedCode", "code"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }

        @Test
        @DisplayName("F25: Validated snippet persists and redirects to /dashboard")
        void testSaveValidatedWithStudentSessionPersistsSnippetAndRedirectsToDashboard() throws Exception {
            MockHttpSession session = createStudentSession("NNM24CS107", "Jeston Correa");

            mockMvc.perform(post("/saveValidated")
                            .session(session)
                            .param("usn", "NNM24CS107")
                            .param("studentName", "Jeston Correa")
                            .param("email", "jeston@nmamit.in")
                            .param("labExpNo", "1")
                            .param("language", "Java")
                            .param("title", "Exp 1 Servlets")
                            .param("rawCode", "<p>test</p>")
                            .param("sanitizedCode", "&lt;p&gt;test&lt;/p&gt;"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));

            List<Snippet> list = snippetRepository.findAll();
            assertEquals(1, list.size());
            Snippet saved = list.get(0);
            assertEquals("NNM24CS107", saved.getUsn());
            assertEquals("&lt;p&gt;test&lt;/p&gt;", saved.getSanitizedCode());
            assertEquals("PENDING", saved.getStatus());
        }

        @Test
        @DisplayName("F25: Saved snippet defaults to PENDING status")
        void testSaveValidatedSetsDefaultStatusPending() throws Exception {
            MockHttpSession session = createStudentSession("NNM24CS108", "Jnanesh Karkera");

            mockMvc.perform(post("/saveValidated")
                            .session(session)
                            .param("usn", "NNM24CS108")
                            .param("studentName", "Jnanesh Karkera")
                            .param("email", "jnanesh@nmamit.in")
                            .param("labExpNo", "2")
                            .param("language", "JSP")
                            .param("title", "Exp 2 Form Validation")
                            .param("rawCode", "int a = 1;")
                            .param("sanitizedCode", "int a = 1;"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));

            Snippet saved = snippetRepository.findAll().get(0);
            assertEquals("PENDING", saved.getStatus());
        }

        @Test
        @DisplayName("F25: Large code block (4,000+ chars) persists completely")
        void testSaveValidatedPersistsLargeCodeBlockWithoutTruncation() throws Exception {
            MockHttpSession session = createStudentSession("NNM24CS107", "Jeston Correa");

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 150; i++) {
                sb.append("// Line ").append(i).append(": int value = ").append(i).append(";\n");
            }
            String code = sb.toString();
            assertTrue(code.length() > 4000);

            mockMvc.perform(post("/saveValidated")
                            .session(session)
                            .param("usn", "NNM24CS107")
                            .param("studentName", "Jeston Correa")
                            .param("email", "jeston@nmamit.in")
                            .param("labExpNo", "1")
                            .param("language", "Java")
                            .param("title", "Large Block")
                            .param("rawCode", code)
                            .param("sanitizedCode", code))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));

            Snippet saved = snippetRepository.findAll().get(0);
            assertEquals(code, saved.getRawCode());
        }
    }

    // =========================================================================
    // 6. REVIEWER REVIEW VIEW GATING (Feature 26)
    // =========================================================================
    @Nested
    @DisplayName("Reviewer Review View Gating (GET /review/{id})")
    class ReviewViewGatingTests {

        @Test
        @DisplayName("F26: Unauthenticated access to /review/{id} redirects to /login")
        void testReviewFormUnauthenticatedRedirectsToLogin() throws Exception {
            mockMvc.perform(get("/review/1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }

        @Test
        @DisplayName("F26: Student role access to /review/{id} is forbidden and redirects to /dashboard")
        void testReviewFormWithStudentRoleRedirectsToDashboard() throws Exception {
            Snippet s = persistSnippet("NNM24CS107", "Jeston Correa", "jeston@nmamit.in", 1, "Java", "Test", "code", "code");
            MockHttpSession session = createStudentSession("NNM24CS107", "Jeston Correa");

            mockMvc.perform(get("/review/" + s.getId()).session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));
        }

        @Test
        @DisplayName("F26: Reviewer role access to /review/{id} returns review_form view with snippet")
        void testReviewFormWithReviewerRoleReturnsReviewFormView() throws Exception {
            Snippet s = persistSnippet("NNM24CS107", "Jeston Correa", "jeston@nmamit.in", 1, "Java", "Test", "code", "code");
            MockHttpSession session = createReviewerSession("admin", "Prof. Akhilraj V. Gadagkar");

            mockMvc.perform(get("/review/" + s.getId()).session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("review_form"))
                    .andExpect(model().attributeExists("snippet"));
        }

        @Test
        @DisplayName("F26: Accessing /review/{id} for non-existent ID redirects to /dashboard safely")
        void testReviewFormWithNonExistentIdRedirectsToDashboard() throws Exception {
            MockHttpSession session = createReviewerSession("admin", "Prof. Akhilraj V. Gadagkar");

            mockMvc.perform(get("/review/99999").session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));
        }
    }

    // =========================================================================
    // 7. REVIEWER GRADING & FEEDBACK (Feature 27)
    // =========================================================================
    @Nested
    @DisplayName("Reviewer Grading and Feedback (POST /saveReview)")
    class ReviewerGradingTests {

        @Test
        @DisplayName("F27: Unauthenticated /saveReview redirects to /login")
        void testSaveReviewUnauthenticatedRedirectsToLogin() throws Exception {
            mockMvc.perform(post("/saveReview")
                            .param("id", "1")
                            .param("status", "APPROVED")
                            .param("marks", "9")
                            .param("reviewerComments", "Great work"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }

        @Test
        @DisplayName("F27: Student role /saveReview is blocked and redirects to /dashboard")
        void testSaveReviewWithStudentRoleRedirectsToDashboard() throws Exception {
            Snippet s = persistSnippet("NNM24CS107", "Jeston Correa", "jeston@nmamit.in", 1, "Java", "Test", "code", "code");
            MockHttpSession session = createStudentSession("NNM24CS107", "Jeston Correa");

            mockMvc.perform(post("/saveReview")
                            .session(session)
                            .param("id", s.getId().toString())
                            .param("status", "APPROVED")
                            .param("marks", "10")
                            .param("reviewerComments", "Self-graded attempt"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));

            Snippet reload = snippetManager.getSnippetById(s.getId());
            assertEquals("PENDING", reload.getStatus(), "Status must not change on unauthorized review");
            assertEquals(0, reload.getMarks());
        }

        @Test
        @DisplayName("F27: Reviewer updates status, marks, and comments successfully")
        void testSaveReviewWithReviewerRoleUpdatesSnippetAndRedirectsToDashboard() throws Exception {
            Snippet s = persistSnippet("NNM24CS108", "Jnanesh Karkera", "jnanesh@nmamit.in", 2, "JSP", "Lab 2", "code", "code");
            MockHttpSession session = createReviewerSession("admin", "Prof. Akhilraj V. Gadagkar");

            mockMvc.perform(post("/saveReview")
                            .session(session)
                            .param("id", s.getId().toString())
                            .param("status", "APPROVED")
                            .param("marks", "9")
                            .param("reviewerComments", "Well documented lab code."))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));

            Snippet updated = snippetManager.getSnippetById(s.getId());
            assertEquals("APPROVED", updated.getStatus());
            assertEquals(9, updated.getMarks());
            assertEquals("Well documented lab code.", updated.getReviewerComments());
        }

        @Test
        @DisplayName("F27: Boundary marks value 0 is accepted")
        void testSaveReviewWithBoundaryMarksZeroAccepted() throws Exception {
            Snippet s = persistSnippet("NNM24CS100", "Ian Roniel Soans", "ian@nmamit.in", 3, "SQL", "Lab 3", "code", "code");
            MockHttpSession session = createReviewerSession("admin", "Prof. Akhilraj V. Gadagkar");

            mockMvc.perform(post("/saveReview")
                            .session(session)
                            .param("id", s.getId().toString())
                            .param("status", "NEEDS_REVISION")
                            .param("marks", "0")
                            .param("reviewerComments", "Redo query logic."))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));

            Snippet updated = snippetManager.getSnippetById(s.getId());
            assertEquals(0, updated.getMarks());
            assertEquals("NEEDS_REVISION", updated.getStatus());
        }

        @Test
        @DisplayName("F27: Boundary marks value 10 is accepted")
        void testSaveReviewWithBoundaryMarksTenAccepted() throws Exception {
            Snippet s = persistSnippet("NNM24CS076", "Dhanush U. Shetty", "dhanush@nmamit.in", 4, "Java", "Lab 4", "code", "code");
            MockHttpSession session = createReviewerSession("admin", "Prof. Akhilraj V. Gadagkar");

            mockMvc.perform(post("/saveReview")
                            .session(session)
                            .param("id", s.getId().toString())
                            .param("status", "APPROVED")
                            .param("marks", "10")
                            .param("reviewerComments", "Perfect score"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));

            Snippet updated = snippetManager.getSnippetById(s.getId());
            assertEquals(10, updated.getMarks());
            assertEquals("APPROVED", updated.getStatus());
        }

        @Test
        @DisplayName("F27: Workflow status NEEDS_REVISION is persisted properly")
        void testSaveReviewStatusNeedsRevisionWorkflow() throws Exception {
            Snippet s = persistSnippet("NNM24CS107", "Jeston Correa", "jeston@nmamit.in", 1, "Java", "Lab 1", "code", "code");
            MockHttpSession session = createReviewerSession("admin", "Prof. Akhilraj V. Gadagkar");

            mockMvc.perform(post("/saveReview")
                            .session(session)
                            .param("id", s.getId().toString())
                            .param("status", "NEEDS_REVISION")
                            .param("marks", "4")
                            .param("reviewerComments", "Fix error handling"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));

            Snippet updated = snippetManager.getSnippetById(s.getId());
            assertEquals("NEEDS_REVISION", updated.getStatus());
            assertEquals(4, updated.getMarks());
        }
    }

    // =========================================================================
    // 8. SNIPPET DELETION ROUTING (Feature 28)
    // =========================================================================
    @Nested
    @DisplayName("Snippet Deletion Routing (GET /delete/{id})")
    class SnippetDeletionRoutingTests {

        @Test
        @DisplayName("F28: Unauthenticated /delete/{id} redirects to /login")
        void testDeleteUnauthenticatedRedirectsToLogin() throws Exception {
            mockMvc.perform(get("/delete/1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }

        @Test
        @DisplayName("F28: Reviewer can delete any snippet")
        void testDeleteSnippetByReviewerDeletesEntityAndRedirectsToDashboard() throws Exception {
            Snippet s = persistSnippet("NNM24CS107", "Jeston Correa", "jeston@nmamit.in", 1, "Java", "To Delete", "code", "code");
            MockHttpSession session = createReviewerSession("admin", "Prof. Akhilraj V. Gadagkar");

            mockMvc.perform(get("/delete/" + s.getId()).session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));

            assertFalse(snippetManager.exists(s.getId()));
        }

        @Test
        @DisplayName("F28: Author student can delete their own snippet")
        void testDeleteSnippetByAuthorStudentSucceeds() throws Exception {
            Snippet s = persistSnippet("NNM24CS107", "Jeston Correa", "jeston@nmamit.in", 1, "Java", "My Snippet", "code", "code");
            MockHttpSession session = createStudentSession("NNM24CS107", "Jeston Correa");

            mockMvc.perform(get("/delete/" + s.getId()).session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));

            assertFalse(snippetManager.exists(s.getId()));
        }

        @Test
        @DisplayName("F28: Non-author student cannot delete another student's snippet")
        void testDeleteSnippetByNonAuthorStudentBlocked() throws Exception {
            Snippet s = persistSnippet("NNM24CS107", "Jeston Correa", "jeston@nmamit.in", 1, "Java", "Jeston's Snippet", "code", "code");
            MockHttpSession session = createStudentSession("NNM24CS108", "Jnanesh Karkera");

            mockMvc.perform(get("/delete/" + s.getId()).session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));

            assertTrue(snippetManager.exists(s.getId()), "Snippet must NOT be deleted by another student");
        }

        @Test
        @DisplayName("F28: Deletion of non-existent snippet redirects safely to /dashboard")
        void testDeleteNonExistentSnippetRedirectsToDashboardSafely() throws Exception {
            MockHttpSession session = createReviewerSession("admin", "Prof. Akhilraj V. Gadagkar");

            mockMvc.perform(get("/delete/99999").session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));
        }
    }
}
