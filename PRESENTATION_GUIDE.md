# DevPulse — Academic Lab Showcase Presentation Guide
**Course:** Java and Spring Framework Lab (CS3603-1)  
**Team ID:** 24CSB06  
**Team Members:**
- Jeston Correa (NNM24CS107)
- Jnanesh Karkera (NNM24CS108)
- Ian Roniel Soans (NNM24CS100)
- Dhanush U. Shetty (NNM24CS076)
**Faculty Evaluator:** Prof. Akhilraj V. Gadagkar

---

## 1. Executive Summary & Lab Curriculum Mapping

**DevPulse** is a lightweight code snippet and lab review hub designed to demonstrate a comprehensive, unified showcase of **Experiments 1a through 5** under strict curriculum constraints:

| Lab Experiment | Curriculum Topic | DevPulse Implementation Feature |
|---|---|---|
| **Exp 1a & 1b** | Servlets, Session Tracking & Invalidation | Pure `SessionServlet` (`@WebServlet("/SessionServlet")`) using `HttpSession`, role assignment (`STUDENT`, `REVIEWER`), session diagnostics JSON (`action=info`), and session destruction (`action=logout`). **Strict constraint: Zero Spring Security.** |
| **Exp 2a & 2b** | JSP Scriptlets, Form Regex Validation & XSS Defense | Server-side procedural regex validation (`ValidationUtil`) for USN (`^NNM\d{2}CS\d{3}$`), name, lab range (1–10), email, and language. Manual HTML entity sanitization (`SanitizationUtil`) escaping `<`, `>`, `&`, `"`, `'` to neutralize malicious scripts. **Strict constraint: Zero Hibernate Validator (`@Valid`).** |
| **Exp 3** | Raw JDBC Integration & Role Dashboards | Raw JDBC `DBUtil` (with auto-fallback between local MySQL and in-memory H2) and `UserDAO` alongside role-based dashboard views (`dashboard.jsp`) using JSP scriptlets (`<% %>`) and expressions (`<%= %>`). |
| **Exp 4** | Spring Data JPA & MVC Routing | `Snippet` JPA entity with identity generation and `@Lob` code storage; `SnippetRepository` extending `JpaRepository`; `SnippetManager` service; and `WebController` for Spring MVC view routing. |
| **Exp 5** | Spring RESTful Web Services | `ApiController` (`@RestController`) exposing full CRUD JSON endpoints at `/api/snippets` with HTTP status codes (200 OK, 201 CREATED, 404 NOT FOUND). |

---

## 2. Live Demo Script (Step-by-Step for Tomorrow)

### Step 1: Show Clean Maven Build & 324 Passing Tests
- **What to say:** *"Good morning Sir. We have engineered DevPulse with an exhaustive test harness verifying all 5 experiments without manual intervention."*
- **Action:** Open terminal in `devpulse/` and execute:
  ```bash
  .\mvnw.cmd test
  ```
- **What to point out on screen:**
  - Notice **324 tests passing with 0 failures, 0 errors**.
  - Showcases unit tests, mock servlet tests, regex boundary tests, JPA transactions, MVC controllers, REST endpoints, and end-to-end multi-user workflows.

---

### Step 2: Start the Application & Explain Architecture
- **Action:**
  ```bash
  .\mvnw.cmd spring-boot:run
  ```
  Open browser at: `http://localhost:8080/`
- **What to say:**
  - *"Notice we configured Spring Boot with embedded Tomcat Jasper (`tomcat-embed-jasper`) so both modern Spring Data JPA/REST and traditional JSP/Servlets run in a single unified deployment."*
  - Shows login page rendered via `login.jsp`.

---

### Step 3: Demonstrate Student Workflow (Exp 1b, 2a, 2b, 4)
1. **Login as Student:**
   - Username: `NNM24CS107`
   - Password: `password123`
   - Click **Sign In**.
2. **Session Lifecycle & Diagnostics (Exp 1a):**
   - Click **Session Info (Exp 1a)** in top navigation bar (`/SessionServlet?action=info`).
   - Point out: Shows active Session ID, creation timestamp, last accessed timestamp, username, role (`STUDENT`), and full name.
   - Click **Back to Dashboard**.
3. **Submit Code Snippet (Exp 2a & 2b):**
   - Click **+ Submit Snippet (Exp 2a)**.
   - Form is pre-populated with student USN and name.
   - Fill in:
     - Email: `jeston@nmamit.in`
     - Lab Exp No: `1`
     - Language: `Java`
     - Title: `Servlet Lifecycle & Session Demo`
     - Code: Paste an intentional XSS script payload:
       ```html
       <script>alert('XSS Vulnerability Test');</script>
       public void doGet(HttpServletRequest req, HttpServletResponse res) {
           System.out.println("Executing Servlet Lab 1");
       }
       ```
   - Click **Validate & Preview (Exp 2b)**.
4. **Show Server-Side Validation & XSS Sanitization:**
   - Point out: All regex validations passed (`^NNM\d{2}CS\d{3}$`).
   - Point out: `<script>` has been transformed to `&lt;script&gt;` so it renders safely as text and does not execute in the browser.
   - Click **Confirm & Save Snippet to Database (Exp 4)**.
   - Returns to dashboard showing snippet with badge `PENDING`.
5. **Logout (Exp 1b):**
   - Click **Logout** (`/SessionServlet?action=logout`).
   - Session is invalidated and redirects to login with green logout banner.

---

### Step 4: Demonstrate Faculty Reviewer Workflow (Exp 3 & 4)
1. **Login as Faculty Reviewer:**
   - Username: `admin`
   - Password: `admin123`
   - Click **Sign In**.
2. **Review Dashboard:**
   - Point out: Reviewer view shows **Review & Grade (Exp 3)** buttons and **Delete** controls.
   - Click **Review & Grade** on the student's submission.
3. **Grade & Feedback Form (`review_form.jsp`):**
   - Status: Select `APPROVED`
   - Marks: Enter `10`
   - Comments: Enter `"Excellent manual session management and XSS defense logic."`
   - Click **Submit Grade & Feedback**.
4. **Dashboard Verification:**
   - Status badge turns green (`APPROVED`).
   - Marks display `10/10` with reviewer comments.

---

### Step 5: Demonstrate REST API (Experiment 5)
Open terminal or browser tab to demonstrate REST endpoints:

- **1. List all snippets (JSON):**
  ```bash
  curl -X GET http://localhost:8080/api/snippets
  ```
  *Returns JSON array with all snippets (status 200 OK).*

- **2. Get snippet by ID:**
  ```bash
  curl -X GET http://localhost:8080/api/snippets/1
  ```
  *Returns single snippet JSON with 200 OK.*

- **3. Test 404 NOT FOUND error handling:**
  ```bash
  curl -i -X GET http://localhost:8080/api/snippets/999999
  ```
  *Returns `HTTP/1.1 404 Not Found` handled by NoSuchElementException translation.*

---

## 3. Potential Evaluator Questions & Model Answers

### Q1: Why did you not use Spring Security?
> **Answer:** *"The CS3603-1 lab syllabus specifically assesses fundamental Servlet session management (Experiment 1b). We deliberately avoided Spring Security to implement raw `HttpSession` handling, session attribute binding (`session.setAttribute("role", ...)`), defense against session fixation by invalidating existing sessions, and explicit logout via `session.invalidate()`."*

### Q2: Why did you not use `@Valid` or Hibernate Validator?
> **Answer:** *"Experiment 2b focuses on mastering Java Regular Expressions and procedural input sanitization. We implemented `ValidationUtil` using compiled `java.util.regex.Pattern` for USN format (`^NNM\d{2}CS\d{3}$`), name validation, and boundary checks (1–10). Additionally, `SanitizationUtil` provides deterministic HTML entity replacement (`&`, `<`, `>`, `"`, `'`) to prevent XSS."*

### Q3: How do you support both JSP and Spring REST in one application?
> **Answer:** *"In `pom.xml`, we added `tomcat-embed-jasper` and JSTL runtime dependencies, and configured `spring.mvc.view.prefix=/WEB-INF/jsp/` and `suffix=.jsp` in `application.properties`. Spring MVC routes (`WebController`) return View names matching `.jsp` templates, while `@RestController` (`ApiController`) writes JSON directly to the HTTP response body via Jackson."*

### Q4: How is database persistence handled across environments?
> **Answer:** *"We implemented dual configuration: local MySQL (`jdbc:mysql://localhost:3306/student`) is used by default in production/lab setups. For automated evaluation and testing, we configured an H2 in-memory test profile (`application-test.properties`) running in MySQL compatibility mode with `create-drop` DDL and seed scripts (`schema.sql`, `data.sql`). Both raw JDBC (`DBUtil`) and JPA (`SnippetRepository`) dynamically respect this profile."*

---

## 4. Key Files Summary

- **Configuration:** `pom.xml`, `application.properties`, `application-test.properties`, `schema.sql`, `data.sql`
- **Models / Entities:** `Snippet.java` (JPA Entity), `User.java` (POJO)
- **Persistence Layer:** `SnippetRepository.java` (`JpaRepository`), `SnippetManager.java` (`@Service`)
- **Raw JDBC & Auth:** `DBUtil.java`, `UserDAO.java`, `SessionServlet.java` (`@WebServlet("/SessionServlet")`)
- **Controllers & Utilities:** `WebController.java`, `ApiController.java`, `ValidationUtil.java`, `SanitizationUtil.java`
- **JSP Views:** `login.jsp`, `dashboard.jsp`, `submit_snippet.jsp`, `submit_result.jsp`, `review_form.jsp`
- **Test Suites:** 9 test classes with **324 tests passing 100%**!
