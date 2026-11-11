# DevPulse — Lightweight Code Snippet & Lab Review Hub

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Build Status](https://img.shields.io/badge/Build-Passing%20(324%20Tests)-success.svg)]()
[![Course](https://img.shields.io/badge/Course-CS3603--1-blue.svg)]()

> **Academic Showcase for Java and Spring Framework Lab (CS3603-1)**  
> **Institution:** NMAM Institute of Technology, Nitte  
> **Team ID:** 24CSB06  
> **Faculty Evaluator:** Prof. Akhilraj V. Gadagkar  

---

## 👥 Team Members

| Name | USN | Contribution / Role |
|---|---|---|
| **Jeston Correa** | `NNM24CS107` | Project Scaffolding, Spring MVC & JSP View Integration |
| **Jnanesh Karkera** | `NNM24CS108` | JPA Persistence Layer, Transactional Service & Entity Mappings |
| **Ian Roniel Soans** | `NNM24CS100` | Raw JDBC Auth, Manual Session Management & SessionServlet |
| **Dhanush U. Shetty** | `NNM24CS076` | Server-Side Regex Validation, XSS Sanitization & RESTful API |

---

## 🎯 Project Overview & Syllabus Mapping

**DevPulse** is a multi-tier web application purpose-built to demonstrate strict compliance with **Experiments 1a through 5** of the CS3603-1 lab curriculum. 

Instead of hiding fundamental concepts behind high-level abstraction frameworks, DevPulse deliberately showcases pure underlying technologies:

```
                                  ┌────────────────────────────────────────┐
                                  │           Browser / Client             │
                                  └───────────────────┬────────────────────┘
                                                      │
                       ┌──────────────────────────────┴──────────────────────────────┐
                       │                                                             │
                       ▼                                                             ▼
         ┌───────────────────────────┐                                 ┌───────────────────────────┐
         │      SessionServlet       │                                 │       WebController       │
         │  (@WebServlet, Exp 1a/1b) │                                 │    (Spring MVC, Exp 4)    │
         └─────────────┬─────────────┘                                 └─────────────┬─────────────┘
                       │                                                             │
         ┌─────────────▼─────────────┐                                 ┌─────────────▼─────────────┐
         │     DBUtil & UserDAO      │                                 │      ValidationUtil &     │
         │     (Raw JDBC, Exp 3)     │                                 │      SanitizationUtil     │
         └─────────────┬─────────────┘                                 │    (Exp 2a/2b Regex/XSS)  │
                       │                                               └─────────────┬─────────────┘
                       │                                                             │
                       │               ┌───────────────────────────┐                 │
                       │               │       ApiController       │                 │
                       │               │     (@RestController)     │                 │
                       │               │       (REST, Exp 5)       │                 │
                       │               └─────────────┬─────────────┘                 │
                       │                             │                               │
                       └─────────────────────────────┼───────────────────────────────┘
                                                     ▼
                                       ┌───────────────────────────┐
                                       │      SnippetManager       │
                                       │   (@Service, Exp 4 & 5)   │
                                       └─────────────┬─────────────┘
                                                     ▼
                                       ┌───────────────────────────┐
                                       │     SnippetRepository     │
                                       │    (JpaRepository<T>)     │
                                       └─────────────┬─────────────┘
                                                     ▼
                                       ┌───────────────────────────┐
                                       │   MySQL / In-Memory H2    │
                                       │       (Dual Profile)      │
                                       └───────────────────────────┘
```

| Lab Experiment | Syllabus Focus | DevPulse Implementation Highlight | Strict Constraint Respected |
|---|---|---|---|
| **Exp 1a** | Servlet Lifecycle & Diagnostics | `/SessionServlet?action=info` exposes Session ID, Creation Timestamp, and Last Accessed Timestamp. | Pure `HttpServlet` without framework interception. |
| **Exp 1b** | Manual Session Tracking & RBAC | `SessionServlet` handles login, binds `username`, `role` (`STUDENT`, `REVIEWER`), session fixation protection, and invalidation on `/SessionServlet?action=logout`. | **Zero Spring Security.** |
| **Exp 2a** | JSP Form Rendering & Formatting | `submit_snippet.jsp` and `submit_result.jsp` using scriptlets (`<% %>`) and expressions (`<%= %>`); metadata formatting composite strings. | Traditional JSP + Servlet execution. |
| **Exp 2b** | Server-Side Regex Validation & XSS Defense | `ValidationUtil` validates USN (`^NNM\d{2}CS\d{3}$`), name, experiment bounds (1–10), and language. `SanitizationUtil` replaces `<`, `>`, `&`, `"`, `'` with HTML entities. | **Zero Hibernate Validator (`@Valid`).** |
| **Exp 3** | Raw JDBC Operations & Role Dashboards | `DBUtil` connection pooling and `UserDAO` queries for credential validation; `dashboard.jsp` with role-restricted UI elements. | Raw JDBC side-by-side with JPA. |
| **Exp 4** | Spring Data JPA Persistence | `Snippet` JPA entity with identity generation and `@Lob` code storage; `SnippetRepository` extending `JpaRepository`; `SnippetManager` service. | Enterprise JPA ORM standards. |
| **Exp 5** | RESTful Web Services | `ApiController` exposing `/api/snippets` with GET, POST, PUT, DELETE, and HTTP status codes (200 OK, 201 CREATED, 404 NOT FOUND). | Clean REST API with JSON payloads. |

---

## 📂 Project Structure

```
JSF Project/
├── devpulse/                                  # Spring Boot Web Application
│   ├── pom.xml                               # Unified Maven descriptor
│   ├── mvnw / mvnw.cmd / .mvn/               # Maven wrapper (Java 21)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/demo/
│   │   │   │   ├── DevPulseApplication.java   # @SpringBootApplication & @ServletComponentScan
│   │   │   │   ├── controller/
│   │   │   │   │   ├── WebController.java     # Spring MVC routing to JSP views
│   │   │   │   │   └── ApiController.java     # Spring @RestController for JSON endpoints
│   │   │   │   ├── dao/
│   │   │   │   │   └── UserDAO.java           # Raw JDBC User data access
│   │   │   │   ├── entity/
│   │   │   │   │   ├── Snippet.java           # JPA Entity (@Entity, @Lob)
│   │   │   │   │   └── User.java              # Authenticated User POJO
│   │   │   │   ├── repository/
│   │   │   │   │   └── SnippetRepository.java # JpaRepository<Snippet, Long>
│   │   │   │   ├── service/
│   │   │   │   │   └── SnippetManager.java    # Transactional service layer
│   │   │   │   ├── servlet/
│   │   │   │   │   └── SessionServlet.java    # @WebServlet("/SessionServlet") auth & lifecycle
│   │   │   │   └── util/
│   │   │   │       ├── DBUtil.java            # Raw JDBC connection manager (MySQL/H2)
│   │   │   │       ├── ValidationUtil.java    # Regex validation logic
│   │   │   │       └── SanitizationUtil.java  # HTML entity escaping
│   │   │   ├── resources/
│   │   │   │   ├── application.properties     # MySQL primary config & JSP resolvers
│   │   │   │   ├── application-test.properties# H2 in-memory test configuration
│   │   │   │   ├── schema.sql                 # DDL for users and snippet tables
│   │   │   │   └── data.sql                   # Seed accounts and initial snippets
│   │   │   └── webapp/WEB-INF/jsp/            # JSP Presentation Layer
│   │   │       ├── login.jsp                  # Authentication view
│   │   │       ├── dashboard.jsp              # Code review dashboard
│   │   │       ├── submit_snippet.jsp         # Code submission form
│   │   │       ├── submit_result.jsp          # Validation errors / preview
│   │   │       └── review_form.jsp            # Reviewer grading form
│   │   └── test/java/com/example/demo/        # 324 Automated Test Cases
│   │       ├── DevPulseApplicationTests.java  # Context loading
│   │       ├── DevPulseE2EWorkflowTest.java   # End-to-end multi-role workflows
│   │       ├── controller/
│   │       │   ├── ApiControllerTest.java     # REST API contract & status tests
│   │       │   └── WebControllerTest.java     # MVC view & route tests
│   │       ├── repository/
│   │       │   ├── SnippetRepositoryTest.java # JPA CRUD tests
│   │       │   └── AdversarialSnippetStressTest.java # Security & boundary tests
│   │       ├── service/
│   │       │   └── SnippetManagerTest.java    # Service transactional tests
│   │       ├── servlet/
│   │       │   └── SessionServletTest.java    # Servlet lifecycle & session tests
│   │       └── util/
│   │           └── ValidationAndSanitizationTest.java # 163 pure regex & XSS tests
├── PRESENTATION_GUIDE.md                     # Step-by-step viva & live demo guide
└── README.md                                  # This documentation
```

---

## 🚀 How to Run the Application

### Prerequisites
- **Java Development Kit (JDK) 21** or later installed.
- Internet connection for Maven dependencies on first run.

### Option A: In-Memory H2 Mode (Recommended for Zero-Setup Demos)
*Runs entirely in-memory without needing a local MySQL server installed. All schema and seed data are initialized automatically.*
```powershell
cd devpulse
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=test
```

### Option B: Local MySQL Mode
*Requires MySQL running on `localhost:3306` with database `student` and user `student`/`student` (or `root`).*
```powershell
cd devpulse
.\mvnw.cmd spring-boot:run
```

Once started, open your web browser at:  
👉 **[http://localhost:8080/](http://localhost:8080/)**

---

## 🔑 Pre-Seeded User Accounts

| Role | Username / USN | Password | Full Name | Capabilities |
|---|---|---|---|---|
| **Student** | `NNM24CS107` | `password123` | Jeston Correa | Submit lab snippets, preview validation/XSS sanitization, view session info, delete own snippets. |
| **Student** | `NNM24CS108` | `password123` | Jnanesh Karkera | Student submission and dashboard access. |
| **Student** | `NNM24CS100` | `password123` | Ian Roniel Soans | Student submission and dashboard access. |
| **Student** | `NNM24CS076` | `password123` | Dhanush U. Shetty | Student submission and dashboard access. |
| **Reviewer** | `admin` | `admin123` | Prof. Akhilraj V. Gadagkar | Review and grade snippets (0–10 marks), write feedback, approve/reject submissions, delete any snippet. |

---

## 🧪 Automated Test Suite (324 Tests Passing Clean)

DevPulse is protected by an automated test suite with **324 tests** achieving **100% pass rate** without human intervention:

```powershell
cd devpulse
.\mvnw.cmd test
```

### Test Suite Distribution
- `ValidationAndSanitizationTest`: **163 tests** covering strict USN formats, name regex, lab bounds (1–10), email patterns, and XSS sanitization.
- `SessionServletTest`: **43 tests** validating manual session lifecycle, diagnostics, role assignment, and logout.
- `WebControllerTest`: **46 tests** verifying public routes, authenticated views, model attributes, and authorization guards.
- `ApiControllerTest`: **21 tests** validating REST CRUD operations, JSON serialization, and HTTP status codes (200, 201, 404).
- `SnippetRepositoryTest` & `SnippetManagerTest`: **26 tests** validating JPA persistence, custom queries, and transaction rollbacks.
- `DevPulseE2EWorkflowTest`: **10 tests** covering complete multi-user submission, grading, and defense workflows.
- `AdversarialSnippetStressTest`: **15 tests** probing LOB boundaries, Unicode characters, SQL symbols, and null-safety.

---

## 🌐 RESTful API Reference (Experiment 5)

| Endpoint | Method | Status Codes | Description | Sample cURL Command |
|---|---|---|---|---|
| `/api/snippets` | `GET` | `200 OK` | Returns JSON array of all snippets. | `curl -X GET http://localhost:8080/api/snippets` |
| `/api/snippets/{id}` | `GET` | `200 OK`, `404 NOT FOUND` | Returns single snippet by ID. | `curl -X GET http://localhost:8080/api/snippets/1` |
| `/api/snippets` | `POST` | `201 CREATED`, `400 BAD REQUEST` | Creates a new snippet with auto-sanitization. | `curl -X POST http://localhost:8080/api/snippets -H "Content-Type: application/json" -d "{\"usn\":\"NNM24CS107\",\"studentName\":\"Jeston Correa\",\"email\":\"jeston@nmamit.in\",\"labExpNo\":1,\"language\":\"Java\",\"title\":\"Servlet Demo\",\"rawCode\":\"int x = 10;\"}"` |
| `/api/snippets/{id}` | `PUT` | `200 OK`, `404 NOT FOUND` | Updates an existing snippet. | `curl -X PUT http://localhost:8080/api/snippets/1 -H "Content-Type: application/json" -d "{\"status\":\"APPROVED\",\"marks\":10}"` |
| `/api/snippets/{id}` | `DELETE` | `200 OK`, `404 NOT FOUND` | Deletes snippet by ID. | `curl -X DELETE http://localhost:8080/api/snippets/1` |

---

## 🎤 Presentation & Viva Walkthrough

A complete, scripted guide for presenting this project to lab evaluators is available in **[`PRESENTATION_GUIDE.md`](./PRESENTATION_GUIDE.md)**, including:
- 30-second project overview pitch.
- Step-by-step live demo sequence for both Student and Reviewer workflows.
- Direct answers to common evaluator viva questions regarding Spring Security avoidance, regex validation, and hybrid JSP/REST architecture.

---

## 📄 License
This project is developed solely for academic demonstration in the **CS3603-1: Java and Spring Framework Lab** course.
