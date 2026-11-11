# Implementation Plan: DevPulse - A Lightweight Code Snippet & Lab Review Hub
**Course:** Java and Spring Framework Lab (CS3603-1)  
**Team ID:** 24CSB06  
**Team Members:** Jeston Correa (NNM24CS107), Jnanesh Karkera (NNM24CS108), Ian Roniel Soans (NNM24CS100), Dhanush U. Shetty (NNM24CS076)

---

## 1. Strict Architectural & Framework Constraints (For AI Code Generator)
To comply strictly with the CS3603-1 Lab curriculum (Experiments 1a through 5), the implementation **MUST ONLY** use the following technologies and coding styles:
1. **No Spring Security:** Role-Based Access Control (RBAC) and authentication must be implemented manually using `HttpSession`, `HttpServletRequest`, and session attributes (`session.getAttribute()`, `session.invalidate()`), identical to **Experiment 1b**.
2. **No Hibernate Validator (`@Valid`):** All input validation (USN format, name format, email, lab experiment numbers) and XSS code sanitization must be done manually using Java Regular Expressions (`java.util.regex.*`, `String.matches()`) and string manipulation, identical to **Experiment 2b**.
3. **Database & Persistence:** Must use MySQL (`jdbc:mysql://localhost:3306/student` with username `root` or `student`). Primary CRUD operations for Code Snippets and Reviews must follow **Experiment 4 & 5** patterns (`@Entity`, `@GeneratedValue(strategy = GenerationType.IDENTITY)`, `JpaRepository`, `@Service` Manager classes). *Optional:* A raw JDBC `DBUtil` & `DAO` class (**Experiment 3** pattern) can be included for user authentication/lookup to showcase pure JDBC integration alongside JPA.
4. **View Layer (JSP + Scriptlets):** As required by the project abstract and **Experiments 2a, 2b, and 3**, views must be rendered using `.jsp` files utilizing standard JSP scriptlets (`<% ... %>`), expressions (`<%= ... %>`), and `request.getParameter()` / `request.getAttribute()`.
5. **RESTful API Layer:** Include a `@RestController` returning `ResponseEntity` and handling `NoSuchElementException` for programmatic CRUD access to snippets (demonstrable via Postman/cURL), strictly following **Experiment 5**.

---

## 2. System Architecture & Project Structure
We will configure a Spring Boot Web Application configured with `tomcat-embed-jasper` to support both **Spring MVC / JPA** and **JSP / Servlet** execution within a single unified project.

```text
devpulse/
├── pom.xml
├── src/main/java/com/example/demo/
│   ├── DevPulseApplication.java          # @SpringBootApplication + @ServletComponentScan
│   ├── entity/
│   │   ├── Snippet.java                  # @Entity (Exp 4 & 5 pattern)
│   │   └── User.java                     # POJO / Model (Exp 3 pattern)
│   ├── repository/
│   │   └── SnippetRepository.java        # extends JpaRepository<Snippet, Long> (Exp 4 & 5)
│   ├── service/
│   │   └── SnippetManager.java           # @Service class encapsulating repo calls (Exp 4 & 5)
│   ├── util/
│   │   └── DBUtil.java                   # Raw JDBC Connection helper (Exp 3 pattern)
│   ├── dao/
│   │   └── UserDAO.java                  # Raw JDBC DAO for User Login/Role verification (Exp 3)
│   ├── servlet/
│   │   └── SessionServlet.java           # @WebServlet("/SessionServlet") for Auth & Session Info (Exp 1a & 1b)
│   └── controller/
│       ├── WebController.java            # @Controller for MVC routing to JSP views (Exp 4)
│       └── ApiController.java            # @RestController for JSON API & Postman testing (Exp 5)
├── src/main/resources/
│   └── application.properties            # MySQL & JSP View Resolver configuration (Exp 4 & 5)
└── src/main/webapp/WEB-INF/jsp/
    ├── login.jsp                         # Login UI posting to SessionServlet (Exp 1b)
    ├── dashboard.jsp                     # Shared Dashboard rendering code & reviews via <% %> (Exp 3)
    ├── submit_snippet.jsp                # Form to submit code snippet (Exp 2a)
    ├── submit_result.jsp                 # Server-side Regex validation & XSS sanitization view (Exp 2b)
    └── review_form.jsp                   # Reviewer grading & feedback form (Exp 3 & 4)
```

---

## 3. Database Schema Setup (MySQL)
Following the lab database conventions, execute these SQL queries in the `student` or `spring` database:

```sql
CREATE DATABASE IF NOT EXISTS student;
USE student;

-- 1. Users Table (Managed via JDBC DAO - Experiment 3 style)
CREATE TABLE users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL, -- 'STUDENT' or 'REVIEWER'
    full_name VARCHAR(100)
);

-- Seed default team members and faculty reviewer
INSERT INTO users VALUES ('NNM24CS107', 'pass107', 'STUDENT', 'Jeston Correa');
INSERT INTO users VALUES ('NNM24CS108', 'pass108', 'STUDENT', 'Jnanesh Karkera');
INSERT INTO users VALUES ('NNM24CS100', 'pass100', 'STUDENT', 'Ian Roniel Soans');
INSERT INTO users VALUES ('NNM24CS076', 'pass076', 'STUDENT', 'Dhanush U. Shetty');
INSERT INTO users VALUES ('admin', 'password', 'REVIEWER', 'Prof. Akhilraj V. Gadagkar');

-- 2. Snippets Table (Managed via Spring Data JPA - Experiment 4 & 5 style)
CREATE TABLE snippet (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usn VARCHAR(20),
    student_name VARCHAR(100),
    email VARCHAR(100),
    lab_exp_no INT,
    language VARCHAR(30),
    title VARCHAR(100),
    raw_code TEXT,
    sanitized_code TEXT,
    status VARCHAR(30),
    reviewer_comments VARCHAR(500),
    marks INT
);
```

---

## 4. Detailed Module-by-Module Implementation Plan

### Phase 1: Maven & Configuration (`pom.xml` & `application.properties`)
1. **Dependencies (`pom.xml`):**
   - `spring-boot-starter-web`
   - `spring-boot-starter-data-jpa`
   - `mysql-connector-j` (Runtime)
   - `org.apache.tomcat.embed:tomcat-embed-jasper` (For JSP rendering)
2. **Application Properties (`src/main/resources/application.properties`):**
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/student
   spring.datasource.username=student
   spring.datasource.password=student
   spring.jpa.hibernate.ddl-auto=update
   spring.mvc.view.prefix=/WEB-INF/jsp/
   spring.mvc.view.suffix=.jsp
   ```
3. **Main Application Class (`DevPulseApplication.java`):**
   - Annotate with `@SpringBootApplication` and `@ServletComponentScan` so `@WebServlet` classes from Experiment 1 are automatically registered.

---

### Phase 2: Authentication & Session Management (Experiments 1a, 1b & 3)
1. **`DBUtil.java` & `UserDAO.java` (Experiment 3 Pattern):**
   - Implement `DBUtil.getConnection()` using `DriverManager.getConnection("jdbc:mysql://localhost:3306/student", "student", "student")`.
   - Implement `UserDAO.validateUser(String username, String password)` using `PreparedStatement` to query the `users` table and return a `User` object containing `username`, `role`, and `fullName`.
2. **`SessionServlet.java` (Experiment 1a & 1b Pattern):**
   - Mapped to `@WebServlet("/SessionServlet")`.
   - **`doGet()`**:
     - Check `String action = request.getParameter("action");`.
     - If `"logout".equals(action)`, fetch `HttpSession session = request.getSession(false);`, call `session.invalidate()`, and redirect to `/login`.
     - If `"info".equals(action)`, use `PrintWriter out = response.getWriter()` to output HTML displaying:
       - Welcome message with `session.getAttribute("username")` and `session.getAttribute("role")`
       - `session.getId()`
       - `new java.util.Date(session.getCreationTime())`
       - `new java.util.Date(session.getLastAccessedTime())`
       - Navigation links to `/dashboard` and `SessionServlet?action=logout`.
   - **`doPost()`**:
     - Read `username` and `password` via `request.getParameter()`.
     - Validate credentials using `UserDAO` (with fallback hardcoded checks for `admin`/`password` as shown in Exp 1b).
     - On success: `HttpSession session = request.getSession(true);`, store `username`, `role`, and `fullName` in session attributes, and `response.sendRedirect("/dashboard")`.
     - On failure: Redirect to `/login?error=true`.

---

### Phase 3: JPA Entity, Repository, and Service Layer (Experiments 4 & 5)
1. **`Snippet.java` (`@Entity`):**
   - Annotate with `@Entity`. Use `@Id` and `@GeneratedValue(strategy = GenerationType.IDENTITY)` on `Long id`.
   - Fields: `Long id`, `String usn`, `String studentName`, `String email`, `int labExpNo`, `String language`, `String title`, `String rawCode`, `String sanitizedCode`, `String status`, `String reviewerComments`, `int marks`.
   - Generate default constructor and standard Getters and Setters (matching `Book.java` and `Mobile.java`).
2. **`SnippetRepository.java` (`@Repository`):**
   - `public interface SnippetRepository extends JpaRepository<Snippet, Long> {}`
3. **`SnippetManager.java` (`@Service`):**
   - `@Autowired SnippetRepository repository;`
   - Methods matching `BookManager` / `MobileManager`:
     - `public List<Snippet> getAllSnippets()` -> `repository.findAll()`
     - `public Snippet getSnippetById(Long id)` -> `repository.findById(id).get()`
     - `public void saveSnippet(Snippet snippet)` -> `repository.save(snippet)`
     - `public void deleteSnippet(Long id)` -> `repository.deleteById(id)`
     - `public Boolean exists(Long id)` -> `repository.existsById(id)`

---

### Phase 4: Spring MVC Controller & Server-Side Validation/Sanitization (Experiments 2a, 2b & 4)
1. **`WebController.java` (`@Controller`):**
   - `@Autowired SnippetManager manager;`
   - `GET /` & `GET /login`: Returns `"login"` view.
   - `GET /dashboard`: Fetches all snippets via `manager.getAllSnippets()`, attaches to `Model` (`model.addAttribute("snippetlist", ...)`), and returns `"dashboard"`.
   - `GET /new`: Checks session role; returns `"submit_snippet"`.
   - `POST /submit`: Routes form parameters to `"submit_result"` JSP (or validates in controller and forwards to `"submit_result"`) to demonstrate **Experiment 2a & 2b** processing.
   - `POST /saveValidated`: Saves the sanitized `Snippet` entity via `manager.saveSnippet(snippet)` and redirects to `"redirect:/dashboard"`.
   - `GET /review/{id}`: Role-restricted to `REVIEWER`. Loads snippet via `manager.getSnippetById(id)`, adds to model, and returns `"review_form"`.
   - `POST /saveReview`: Updates `status`, `reviewerComments`, and `marks` on the `Snippet` and redirects to `"redirect:/dashboard"`.
   - `GET /delete/{id}`: Calls `manager.deleteSnippet(id)` and redirects to `"redirect:/dashboard"`.

2. **Server-Side Regex Validation & XSS Sanitization (`submit_result.jsp` - Experiment 2b Pattern):**
   - Retrieve fields using `request.getParameter()`: `usn`, `studentName`, `email`, `labExpNo`, `language`, `title`, `rawCode`.
   - Perform exact **Experiment 2b** style validation flags (`boolean isValid = true;`):
     - **USN Validation:** `if (usn == null || !usn.matches("^NNM\\d{2}CS\\d{3}$"))` -> Error: `"USN must follow valid format (e.g., NNM24CS107)."`
     - **Student Name Validation:** `if (studentName == null || !studentName.matches("^[a-zA-Z\\s.]+$"))` -> Error: `"Name must contain only letters and spaces."`
     - **Lab Experiment Number Validation:** Parse integer and check `if (labExpNo < 1 || labExpNo > 10)` -> Error: `"Lab Experiment No. must be a valid numeric value between 1 and 10."`
     - **Language Predefined List Validation:** `if (language == null || !language.matches("Java|JSP|SQL|HTML"))` -> Error: `"Invalid language selected."`
     - **Email Validation:** `if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))` -> Error: `"Email must be in a valid format (e.g., name@nmamit.in)."`
     - **Code Non-Empty Check:** `if (rawCode == null || rawCode.trim().isEmpty())` -> Error: `"Code snippet cannot be empty."`
   - **Input Sanitization (XSS Prevention) & String Formatting (Experiment 2a Pattern):**
     - Sanitize `rawCode` before storing/rendering so `<script>` or HTML tags in JSP/Servlet submissions do not break the DOM or execute maliciously:
       ```java
       String sanitizedCode = rawCode.replace("&", "&amp;")
                                     .replace("<", "&lt;")
                                     .replace(">", "&gt;")
                                     .replace("\"", "&quot;")
                                     .replace("'", "&#x27;");
       ```
     - Format metadata string (Exp 2a style): `String authorAndUsn = studentName + " (" + usn + ")";`
     - Format email to lowercase: `String formattedEmail = email.toLowerCase();`
   - If `isValid == false`: Display a `<h3>Validation Errors</h3>` bulleted list next to each field name and a link to go back, identical to `employeeResult.jsp` in **Experiment 2b**.
   - If `isValid == true`: Display the processed preview table, sanitized code block inside `<pre><code><%= sanitizedCode %></code></pre>`, and confirm saving to the database.

---

### Phase 5: Dynamic Dashboard & Role-Based Views (Experiment 3 & 4)
1. **`dashboard.jsp`:**
   - Read current user role from `HttpSession`: `String role = (String) session.getAttribute("role");`.
   - Display a top navigation bar showing logged-in user details, link to Session Info (`/SessionServlet?action=info`), `"Submit New Lab Snippet"` button (if `STUDENT`), and `"Logout"` (`/SessionServlet?action=logout`).
   - Iterate through snippets using JSP scriptlets (`for (Snippet s : snippetlist) { ... }`):
     - Render ID, USN, Student Name, Lab Exp No, Language badge, Title, and Status badge (`PENDING`, `APPROVED`, `NEEDS_REVISION`).
     - Render the sanitized code inside a syntax-styled `<pre><code><%= s.getSanitizedCode() %></code></pre>` block.
     - Render Reviewer Feedback & Marks (`<%= s.getMarks() %>/10`).
     - **Role-Based Action Buttons:**
       - If `"REVIEWER".equals(role)`: Show `<a href="/review/<%= s.getId() %>">Review & Grade</a>` and `<a href="/delete/<%= s.getId() %>">Delete</a>`.
       - If `"STUDENT".equals(role)` and USN matches session username: Show status and allow deletion/resubmission.

---

### Phase 6: RESTful API Controller for Snippets (Experiment 5 Pattern)
1. **`ApiController.java` (`@RestController`):**
   - Implement exact endpoints mirroring `AppController.java` from **Experiment 5** (`Mobile` CRUD):
     - `@GetMapping("/api/snippets")`: `public List<Snippet> listAllSnippets()`
     - `@GetMapping("/api/snippets/{id}")`: Uses `try { ... return new ResponseEntity<Snippet>(snippet, HttpStatus.OK); } catch (NoSuchElementException e) { return new ResponseEntity<Snippet>(HttpStatus.NOT_FOUND); }`
     - `@PostMapping("/api/snippets")`: `public void saveSnippet(@RequestBody Snippet snippet)` (Applies XSS sanitization before saving)
     - `@PutMapping("/api/snippets")`: `public void updateSnippet(@RequestBody Snippet snippet)`
     - `@DeleteMapping("/api/snippets/{id}")`: Checks `if (manager.exists(id))` -> deletes and returns `HttpStatus.OK`, else returns `HttpStatus.NOT_FOUND`.

---

## 5. Verification & Demonstration Checklist (cURL, Postman & Browser)
To demonstrate all lab outcomes during evaluation:

1. **Experiment 1a & 1b Verification (Servlets & Session Tracking):**
   - Navigate to `http://localhost:8080/login`. Log in as `NNM24CS107` / `pass107` or `admin` / `password`.
   - Click **Session Info** (`/SessionServlet?action=info`) to demonstrate active `HttpSession` ID, creation timestamp, last accessed timestamp, and logout invalidation.
2. **Experiment 2a & 2b Verification (JSP Scriptlets, Regex Validation & XSS Sanitization):**
   - Log in as a Student and open `/new`.
   - Test invalid inputs (e.g., USN = `123`, Lab Exp = `25`, Name = `Jeston_123`) to trigger the `Validation Errors` list on `submit_result.jsp`.
   - Submit valid data containing raw HTML/JS tags like `<script>alert('xss')</script>` inside the code box. Verify that `submit_result.jsp` sanitizes the string into `&lt;script&gt;...` and displays it safely without executing the script.
3. **Experiment 3 & 4 Verification (MySQL Persistence, Spring MVC & Role-Based Review):**
   - Log out and log in as `admin` (`REVIEWER`).
   - Click **Review & Grade** on the submitted snippet, enter marks (`9/10`) and feedback, and submit. Verify instant update on the dashboard.
4. **Experiment 5 Verification (REST API & Exception Handling via cURL / Postman):**
   - **GET all snippets:**
     ```bash
     curl -X GET http://localhost:8080/api/snippets
     ```
   - **GET single snippet (Valid ID -> 200 OK, Invalid ID -> 404 NOT_FOUND):**
     ```bash
     curl -i -X GET http://localhost:8080/api/snippets/1
     curl -i -X GET http://localhost:8080/api/snippets/999
     ```
   - **DELETE snippet:**
     ```bash
     curl -i -X DELETE http://localhost:8080/api/snippets/1
     ```