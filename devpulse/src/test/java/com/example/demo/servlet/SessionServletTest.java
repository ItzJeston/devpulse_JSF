package com.example.demo.servlet;

import com.example.demo.dao.UserDAO;
import com.example.demo.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for SessionServlet, UserDAO, and User entity.
 * Exercises Experiments 1a, 1b, and 3: manual session lifecycle, RBAC,
 * diagnostics, fallback authentication, and defensive boundary handling.
 */
@SpringBootTest
@ActiveProfiles("test")
public class SessionServletTest {

    private SessionServlet servlet;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    public void setUp() throws ServletException {
        servlet = new SessionServlet();
        servlet.init();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    // =========================================================================
    // 1. LIFECYCLE & REGISTRATION TESTS (Tier 1)
    // =========================================================================
    @Nested
    @DisplayName("Servlet Registration and Lifecycle")
    class LifecycleAndRegistrationTests {

        @Test
        @DisplayName("F6: Verify @WebServlet annotation is mapped to /SessionServlet")
        void testServletAnnotationAndMapping() {
            WebServlet webServlet = SessionServlet.class.getAnnotation(WebServlet.class);
            assertNotNull(webServlet, "SessionServlet must be annotated with @WebServlet");
            boolean mapped = false;
            for (String urlPattern : webServlet.value()) {
                if ("/SessionServlet".equals(urlPattern)) mapped = true;
            }
            for (String urlPattern : webServlet.urlPatterns()) {
                if ("/SessionServlet".equals(urlPattern)) mapped = true;
            }
            assertTrue(mapped, "@WebServlet must specify URL pattern '/SessionServlet'");
        }

        @Test
        @DisplayName("F6: Verify servlet initialization executes without error")
        void testServletInitLifecycle() {
            assertDoesNotThrow(() -> servlet.init());
        }
    }

    // =========================================================================
    // 2. STUDENT AUTHENTICATION TESTS (Tier 1 & Tier 2)
    // =========================================================================
    @Nested
    @DisplayName("Student Authentication Lifecycle")
    class StudentAuthenticationTests {

        @Test
        @DisplayName("F7 [Tier 1]: Valid Student Login - Jeston Correa (NNM24CS107)")
        void testStudentLoginSuccess_JestonCorrea() throws ServletException, IOException {
            request.setParameter("username", "NNM24CS107");
            request.setParameter("password", "pass107");

            servlet.doPost(request, response);

            assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, response.getStatus());
            assertNotNull(response.getRedirectedUrl());
            assertTrue(response.getRedirectedUrl().contains("/dashboard"));

            HttpSession session = request.getSession(false);
            assertNotNull(session, "Active session must be created on successful login");
            assertEquals("NNM24CS107", session.getAttribute("username"));
            assertEquals("STUDENT", session.getAttribute("role"));
            assertEquals("Jeston Correa", session.getAttribute("fullName"));
        }

        @Test
        @DisplayName("F7 [Tier 1]: Valid Student Login - Jnanesh Karkera (NNM24CS108)")
        void testStudentLoginSuccess_JnaneshKarkera() throws ServletException, IOException {
            request.setParameter("username", "NNM24CS108");
            request.setParameter("password", "pass108");

            servlet.doPost(request, response);

            assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, response.getStatus());
            assertTrue(response.getRedirectedUrl().contains("/dashboard"));
            HttpSession session = request.getSession(false);
            assertNotNull(session);
            assertEquals("NNM24CS108", session.getAttribute("username"));
            assertEquals("STUDENT", session.getAttribute("role"));
            assertEquals("Jnanesh Karkera", session.getAttribute("fullName"));
        }

        @Test
        @DisplayName("F7 [Tier 1]: Valid Student Login - Ian Roniel Soans (NNM24CS100)")
        void testStudentLoginSuccess_IanSoans() throws ServletException, IOException {
            request.setParameter("username", "NNM24CS100");
            request.setParameter("password", "pass100");

            servlet.doPost(request, response);

            assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, response.getStatus());
            assertTrue(response.getRedirectedUrl().contains("/dashboard"));
            HttpSession session = request.getSession(false);
            assertNotNull(session);
            assertEquals("NNM24CS100", session.getAttribute("username"));
            assertEquals("STUDENT", session.getAttribute("role"));
            assertEquals("Ian Roniel Soans", session.getAttribute("fullName"));
        }

        @Test
        @DisplayName("F7 [Tier 1]: Valid Student Login - Dhanush U. Shetty (NNM24CS076)")
        void testStudentLoginSuccess_DhanushShetty() throws ServletException, IOException {
            request.setParameter("username", "NNM24CS076");
            request.setParameter("password", "pass076");

            servlet.doPost(request, response);

            assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, response.getStatus());
            assertTrue(response.getRedirectedUrl().contains("/dashboard"));
            HttpSession session = request.getSession(false);
            assertNotNull(session);
            assertEquals("NNM24CS076", session.getAttribute("username"));
            assertEquals("STUDENT", session.getAttribute("role"));
            assertEquals("Dhanush U. Shetty", session.getAttribute("fullName"));
        }

        @Test
        @DisplayName("F7 [Tier 1]: Ensure session attributes are stored strictly as Strings")
        void testStudentSessionAttributeTypesAndValues() throws ServletException, IOException {
            request.setParameter("username", "NNM24CS107");
            request.setParameter("password", "pass107");

            servlet.doPost(request, response);

            HttpSession session = request.getSession(false);
            assertNotNull(session);
            assertInstanceOf(String.class, session.getAttribute("username"));
            assertInstanceOf(String.class, session.getAttribute("role"));
            assertInstanceOf(String.class, session.getAttribute("fullName"));
        }

        @Test
        @DisplayName("F7 [Tier 2]: Session Fixation Protection - Invalidate pre-existing session")
        void testSessionFixationProtection_OldSessionInvalidated() throws ServletException, IOException {
            MockHttpSession preExistingSession = new MockHttpSession();
            preExistingSession.setAttribute("stale_token", "malicious_session_fixation");
            String preId = preExistingSession.getId();
            request.setSession(preExistingSession);

            request.setParameter("username", "NNM24CS107");
            request.setParameter("password", "pass107");

            servlet.doPost(request, response);

            assertTrue(preExistingSession.isInvalid(), "Pre-existing session must be invalidated");
            HttpSession newSession = request.getSession(false);
            assertNotNull(newSession);
            assertNotEquals(preId, newSession.getId(), "New session ID must be generated");
            assertEquals("NNM24CS107", newSession.getAttribute("username"));
        }

        @Test
        @DisplayName("F7 [Tier 2]: Student Username Case Sensitivity Rejection")
        void testStudentLogin_CaseSensitivity() throws ServletException, IOException {
            request.setParameter("username", "nnm24cs107"); // Lowercase USN
            request.setParameter("password", "pass107");

            servlet.doPost(request, response);

            assertNotNull(response.getRedirectedUrl());
            assertTrue(response.getRedirectedUrl().contains("/login?error=true"));
        }
    }

    // =========================================================================
    // 3. REVIEWER & FALLBACK AUTHENTICATION TESTS (Tier 1 & Tier 2)
    // =========================================================================
    @Nested
    @DisplayName("Reviewer and Emergency Fallback Authentication")
    class ReviewerAndFallbackAuthenticationTests {

        @Test
        @DisplayName("F10 [Tier 1]: Reviewer Login with fallback admin/password")
        void testReviewerLoginSuccess_AdminFallback() throws ServletException, IOException {
            request.setParameter("username", "admin");
            request.setParameter("password", "password");

            servlet.doPost(request, response);

            assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, response.getStatus());
            assertNotNull(response.getRedirectedUrl());
            assertTrue(response.getRedirectedUrl().contains("/dashboard"));

            HttpSession session = request.getSession(false);
            assertNotNull(session);
            assertEquals("admin", session.getAttribute("username"));
            assertEquals("REVIEWER", session.getAttribute("role"));
            assertEquals("Prof. Akhilraj V. Gadagkar", session.getAttribute("fullName"));
        }

        @Test
        @DisplayName("F10 [Tier 2]: Fallback Auth Case Sensitivity - Admin vs admin")
        void testFallbackAuth_CaseSensitivityRejection() throws ServletException, IOException {
            request.setParameter("username", "Admin");
            request.setParameter("password", "password");

            servlet.doPost(request, response);

            assertNotNull(response.getRedirectedUrl());
            assertTrue(response.getRedirectedUrl().contains("/login?error=true"));
            HttpSession session = request.getSession(false);
            assertTrue(session == null || session.getAttribute("username") == null);
        }

        @Test
        @DisplayName("F10 [Tier 2]: Fallback Auth Whitespace Sensitivity")
        void testFallbackAuth_WhitespaceRejection() throws ServletException, IOException {
            request.setParameter("username", "admin ");
            request.setParameter("password", "password");

            servlet.doPost(request, response);

            assertNotNull(response.getRedirectedUrl());
            assertTrue(response.getRedirectedUrl().contains("/login?error=true"));
        }
    }

    // =========================================================================
    // 4. AUTHENTICATION FAILURE & CORNER CASES (Tier 2)
    // =========================================================================
    @Nested
    @DisplayName("Authentication Boundary & Failure Handling")
    class AuthenticationFailureAndBoundaryTests {

        @Test
        @DisplayName("F7 [Tier 2]: Invalid Password for Existing User")
        void testLoginFailure_IncorrectPassword() throws ServletException, IOException {
            request.setParameter("username", "NNM24CS107");
            request.setParameter("password", "wrongpassword");

            servlet.doPost(request, response);

            assertNotNull(response.getRedirectedUrl());
            assertTrue(response.getRedirectedUrl().contains("/login?error=true"));
            HttpSession session = request.getSession(false);
            assertTrue(session == null || session.getAttribute("username") == null);
        }

        @Test
        @DisplayName("F7 [Tier 2]: Non-Existent Username")
        void testLoginFailure_NonExistentUser() throws ServletException, IOException {
            request.setParameter("username", "UNKNOWN_STUDENT");
            request.setParameter("password", "password");

            servlet.doPost(request, response);

            assertNotNull(response.getRedirectedUrl());
            assertTrue(response.getRedirectedUrl().contains("/login?error=true"));
        }

        @ParameterizedTest
        @CsvSource({
            "'', 'pass107'",
            "'NNM24CS107', ''",
            "'', ''"
        })
        @DisplayName("F7 [Tier 2]: Empty Username or Password")
        void testLoginFailure_EmptyUsernameOrPassword(String u, String p) throws ServletException, IOException {
            request.setParameter("username", u);
            request.setParameter("password", p);

            servlet.doPost(request, response);

            assertNotNull(response.getRedirectedUrl());
            assertTrue(response.getRedirectedUrl().contains("/login?error=true"));
        }

        @Test
        @DisplayName("F7 [Tier 2]: Null Parameters on Request Body")
        void testLoginFailure_NullParameters() throws ServletException, IOException {
            // No parameters set on request
            servlet.doPost(request, response);

            assertNotNull(response.getRedirectedUrl());
            assertTrue(response.getRedirectedUrl().contains("/login?error=true"));
        }

        @Test
        @DisplayName("F7 [Tier 2]: Whitespace-only Parameters")
        void testLoginFailure_WhitespaceOnlyParameters() throws ServletException, IOException {
            request.setParameter("username", "   ");
            request.setParameter("password", "   ");

            servlet.doPost(request, response);

            assertNotNull(response.getRedirectedUrl());
            assertTrue(response.getRedirectedUrl().contains("/login?error=true"));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "' OR '1'='1' --",
            "admin' --",
            "' UNION SELECT * FROM users --"
        })
        @DisplayName("F7 [Tier 2]: SQL Injection Payload Neutralized via PreparedStatement")
        void testLoginFailure_SqlInjectionNeutralized(String sqlPayload) throws ServletException, IOException {
            request.setParameter("username", sqlPayload);
            request.setParameter("password", "password");

            servlet.doPost(request, response);

            assertNotNull(response.getRedirectedUrl());
            assertTrue(response.getRedirectedUrl().contains("/login?error=true"));
            HttpSession session = request.getSession(false);
            assertTrue(session == null || session.getAttribute("username") == null);
        }
    }

    // =========================================================================
    // 5. SESSION LOGOUT & INVALIDATION (Tier 1 & Tier 2)
    // =========================================================================
    @Nested
    @DisplayName("Session Logout & Invalidation Lifecycle")
    class SessionLogoutTests {

        @Test
        @DisplayName("F8 [Tier 1]: Logout invalidates active student session")
        void testLogoutSuccess_ActiveStudentSession() throws ServletException, IOException {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("username", "NNM24CS107");
            session.setAttribute("role", "STUDENT");
            request.setSession(session);
            request.setParameter("action", "logout");

            servlet.doGet(request, response);

            assertTrue(session.isInvalid(), "Active session must be invalidated on logout");
            assertNotNull(response.getRedirectedUrl());
            assertTrue(response.getRedirectedUrl().contains("/login"));
        }

        @Test
        @DisplayName("F8 [Tier 1]: Logout invalidates active reviewer session")
        void testLogoutSuccess_ActiveReviewerSession() throws ServletException, IOException {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("username", "admin");
            session.setAttribute("role", "REVIEWER");
            request.setSession(session);
            request.setParameter("action", "logout");

            servlet.doGet(request, response);

            assertTrue(session.isInvalid());
            assertNotNull(response.getRedirectedUrl());
            assertTrue(response.getRedirectedUrl().contains("/login"));
        }

        @Test
        @DisplayName("F8 [Tier 1]: Session attributes throw IllegalStateException post-invalidation")
        void testLogout_AttributesInaccessiblePostInvalidation() throws ServletException, IOException {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("username", "NNM24CS107");
            request.setSession(session);
            request.setParameter("action", "logout");

            servlet.doGet(request, response);

            assertThrows(IllegalStateException.class, () -> session.getAttribute("username"));
        }

        @Test
        @DisplayName("F8 [Tier 1]: Logout returns 302 Redirect Status")
        void testLogout_RedirectStatusCodeAndHeader() throws ServletException, IOException {
            MockHttpSession session = new MockHttpSession();
            request.setSession(session);
            request.setParameter("action", "logout");

            servlet.doGet(request, response);

            assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, response.getStatus());
            assertNotNull(response.getRedirectedUrl());
        }

        @Test
        @DisplayName("F8 [Tier 2]: Logout when request has null session does not throw NPE")
        void testLogout_NullSessionHandling() {
            request.setSession(null);
            request.setParameter("action", "logout");

            assertDoesNotThrow(() -> servlet.doGet(request, response));
            assertNotNull(response.getRedirectedUrl());
            assertTrue(response.getRedirectedUrl().contains("/login"));
        }

        @Test
        @DisplayName("F8 [Tier 2]: Logout when session is already invalidated does not crash")
        void testLogout_AlreadyInvalidatedSessionHandling() {
            MockHttpSession session = new MockHttpSession();
            session.invalidate();
            request.setSession(session);
            request.setParameter("action", "logout");

            assertDoesNotThrow(() -> servlet.doGet(request, response));
            assertNotNull(response.getRedirectedUrl());
            assertTrue(response.getRedirectedUrl().contains("/login"));
        }

        @Test
        @DisplayName("F8 [Tier 2]: Logout with extra query parameters still invalidates")
        void testLogout_ExtraQueryParametersIgnored() throws ServletException, IOException {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("username", "NNM24CS107");
            request.setSession(session);
            request.setParameter("action", "logout");
            request.setParameter("extraParam", "ignoreMe");

            servlet.doGet(request, response);

            assertTrue(session.isInvalid());
            assertNotNull(response.getRedirectedUrl());
            assertTrue(response.getRedirectedUrl().contains("/login"));
        }
    }

    // =========================================================================
    // 6. SESSION DIAGNOSTICS INFO TESTS (Tier 1 & Tier 2)
    // =========================================================================
    @Nested
    @DisplayName("Session Diagnostics Info (/SessionServlet?action=info)")
    class SessionDiagnosticsInfoTests {

        @Test
        @DisplayName("F9 [Tier 1]: Render diagnostics output for active student session")
        void testSessionInfo_StudentDiagnosticsRendered() throws ServletException, IOException {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("username", "NNM24CS107");
            session.setAttribute("role", "STUDENT");
            session.setAttribute("fullName", "Jeston Correa");
            request.setSession(session);
            request.setParameter("action", "info");

            servlet.doGet(request, response);

            assertNotNull(response.getContentType());
            assertTrue(response.getContentType().contains("text/html"));
            String html = response.getContentAsString();
            assertTrue(html.contains(session.getId()), "HTML must include Session ID");
            assertTrue(html.contains("NNM24CS107"), "HTML must display username");
            assertTrue(html.contains("STUDENT"), "HTML must display role");
            assertTrue(html.contains("Jeston Correa"), "HTML must display full name");
        }

        @Test
        @DisplayName("F9 [Tier 1]: Render diagnostics output for active reviewer session")
        void testSessionInfo_ReviewerDiagnosticsRendered() throws ServletException, IOException {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("username", "admin");
            session.setAttribute("role", "REVIEWER");
            session.setAttribute("fullName", "Prof. Akhilraj V. Gadagkar");
            request.setSession(session);
            request.setParameter("action", "info");

            servlet.doGet(request, response);

            String html = response.getContentAsString();
            assertTrue(html.contains("admin"));
            assertTrue(html.contains("REVIEWER"));
            assertTrue(html.contains("Prof. Akhilraj V. Gadagkar"));
        }

        @Test
        @DisplayName("F9 [Tier 1]: Verify Creation Time and Last Access Time in HTML")
        void testSessionInfo_TimestampsRendered() throws ServletException, IOException {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("username", "NNM24CS107");
            session.setAttribute("role", "STUDENT");
            request.setSession(session);
            request.setParameter("action", "info");

            servlet.doGet(request, response);

            String html = response.getContentAsString();
            assertFalse(html.isEmpty());
            assertTrue(html.toLowerCase().contains("creation") || html.contains(String.valueOf(session.getCreationTime())));
            assertTrue(html.toLowerCase().contains("access") || html.contains(String.valueOf(session.getLastAccessedTime())));
        }

        @Test
        @DisplayName("F9 [Tier 1]: Verify Navigation Links to Dashboard and Logout")
        void testSessionInfo_NavigationLinksRendered() throws ServletException, IOException {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("username", "NNM24CS107");
            request.setSession(session);
            request.setParameter("action", "info");

            servlet.doGet(request, response);

            String html = response.getContentAsString();
            assertTrue(html.contains("/dashboard"), "Must contain link to /dashboard");
            assertTrue(html.contains("SessionServlet?action=logout") || html.contains("action=logout"), "Must contain link to logout");
        }

        @Test
        @DisplayName("F9 [Tier 2]: Diagnostics without active session handled gracefully without NPE")
        void testSessionInfo_NullSessionHandling() {
            request.setSession(null);
            request.setParameter("action", "info");

            assertDoesNotThrow(() -> servlet.doGet(request, response));
            String redirect = response.getRedirectedUrl();
            String html = response.getContentAsString();
            assertTrue(redirect != null && redirect.contains("/login") || html.contains("No active session") || html.contains("login") || html.isEmpty());
        }

        @Test
        @DisplayName("F9 [Tier 2]: Diagnostics for session without username attribute")
        void testSessionInfo_AnonymousSessionWithoutAttributes() {
            MockHttpSession emptySession = new MockHttpSession();
            request.setSession(emptySession);
            request.setParameter("action", "info");

            assertDoesNotThrow(() -> servlet.doGet(request, response));
        }

        @Test
        @DisplayName("F9 [Tier 2]: Null or unrecognized action parameter handling")
        void testSessionInfo_NullOrUnknownActionHandling() throws ServletException, IOException {
            request.setParameter("action", (String) null);
            assertDoesNotThrow(() -> servlet.doGet(request, response));

            response = new MockHttpServletResponse();
            request.setParameter("action", "unrecognizedAction123");
            assertDoesNotThrow(() -> servlet.doGet(request, response));
        }
    }

    // =========================================================================
    // 7. USER POJO & USER DAO DIRECT INTEGRATION (Tier 1 & Tier 2)
    // =========================================================================
    @Nested
    @DisplayName("User Model and UserDAO Direct Tests")
    class UserModelAndDaoDirectTests {

        @Test
        @DisplayName("F3 [Tier 1]: User POJO getters and setters")
        void testUserModelGettersAndSetters() {
            User user = new User("NNM24CS107", "pass107", "STUDENT", "Jeston Correa");
            assertEquals("NNM24CS107", user.getUsername());
            assertEquals("pass107", user.getPassword());
            assertEquals("STUDENT", user.getRole());
            assertEquals("Jeston Correa", user.getFullName());

            user.setUsername("admin");
            user.setRole("REVIEWER");
            assertEquals("admin", user.getUsername());
            assertEquals("REVIEWER", user.getRole());
        }

        @Test
        @DisplayName("F5 [Tier 1]: UserDAO validateUser returns valid User model")
        void testUserDaoDirectValidationSuccess() {
            UserDAO dao = new UserDAO();
            User user = dao.validateUser("NNM24CS107", "pass107");
            assertNotNull(user);
            assertEquals("NNM24CS107", user.getUsername());
            assertEquals("STUDENT", user.getRole());
        }

        @Test
        @DisplayName("F5 [Tier 2]: UserDAO validateUser returns null on invalid password")
        void testUserDaoDirectValidationFailure() {
            UserDAO dao = new UserDAO();
            User user = dao.validateUser("NNM24CS107", "wrongpassword");
            assertNull(user);
        }
    }
}
