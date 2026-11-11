package com.example.demo.controller;

import com.example.demo.entity.Snippet;
import com.example.demo.service.SnippetManager;
import com.example.demo.util.SanitizationUtil;
import com.example.demo.util.ValidationUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Spring MVC Web Controller (Experiment 4) providing routing to JSP views,
 * manual session-based access control, input validation, and review workflows.
 */
@Controller
public class WebController {

    private final SnippetManager snippetManager;

    @Autowired
    public WebController(SnippetManager snippetManager) {
        this.snippetManager = snippetManager;
    }

    private boolean isAuthenticated(HttpSession session) {
        if (session == null) {
            return false;
        }
        Object username = session.getAttribute("username");
        return username instanceof String && !((String) username).trim().isEmpty();
    }

    @GetMapping("/")
    public String root(HttpSession session) {
        if (isAuthenticated(session)) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            HttpSession session,
            Model model) {

        if (isAuthenticated(session)) {
            return "redirect:/dashboard";
        }
        if (error != null) {
            model.addAttribute("error", true);
        }
        if (logout != null) {
            model.addAttribute("logout", true);
        }
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }
        model.addAttribute("snippetlist", snippetManager.getAllSnippets());
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("role", session.getAttribute("role"));
        model.addAttribute("fullName", session.getAttribute("fullName"));
        return "dashboard";
    }

    @GetMapping("/new")
    public String newSnippet(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }
        String role = (String) session.getAttribute("role");
        if ("REVIEWER".equalsIgnoreCase(role)) {
            return "redirect:/dashboard";
        }
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("fullName", session.getAttribute("fullName"));
        return "submit_snippet";
    }

    @PostMapping("/submit")
    public String submit(
            @RequestParam(value = "usn", required = false) String usn,
            @RequestParam(value = "studentName", required = false) String studentName,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "labExpNo", required = false) String labExpNo,
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "rawCode", required = false) String rawCode,
            Model model) {

        List<String> errors = ValidationUtil.validateSnippetSubmission(
                usn, studentName, email, labExpNo, language, title, rawCode);

        model.addAttribute("usn", usn);
        model.addAttribute("studentName", studentName);
        model.addAttribute("email", email);
        model.addAttribute("labExpNo", labExpNo);
        model.addAttribute("language", language);
        model.addAttribute("title", title);
        model.addAttribute("rawCode", rawCode);

        if (errors.isEmpty()) {
            model.addAttribute("isValid", true);
            String sanitized = SanitizationUtil.sanitizeHtml(rawCode);
            model.addAttribute("sanitizedCode", sanitized);
        } else {
            model.addAttribute("isValid", false);
            model.addAttribute("errors", errors);
        }
        return "submit_result";
    }

    @PostMapping("/saveValidated")
    public String saveValidated(
            @RequestParam("usn") String usn,
            @RequestParam("studentName") String studentName,
            @RequestParam("email") String email,
            @RequestParam("labExpNo") String labExpNo,
            @RequestParam("language") String language,
            @RequestParam("title") String title,
            @RequestParam("rawCode") String rawCode,
            @RequestParam(value = "sanitizedCode", required = false) String sanitizedCode,
            HttpSession session) {

        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }

        Snippet snippet = new Snippet();
        snippet.setUsn(usn);
        snippet.setStudentName(studentName);
        snippet.setEmail(email);
        try {
            snippet.setLabExpNo(Integer.parseInt(labExpNo.trim()));
        } catch (Exception e) {
            snippet.setLabExpNo(1);
        }
        snippet.setLanguage(language);
        snippet.setTitle(title);
        snippet.setRawCode(rawCode);
        snippet.setSanitizedCode(sanitizedCode != null && !sanitizedCode.isEmpty()
                ? sanitizedCode
                : SanitizationUtil.sanitizeHtml(rawCode));
        snippet.setStatus("PENDING");
        snippet.setMarks(0);

        snippetManager.saveSnippet(snippet);
        return "redirect:/dashboard";
    }

    @GetMapping("/review/{id}")
    public String reviewForm(@PathVariable("id") Long id, HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }
        String role = (String) session.getAttribute("role");
        if (!"REVIEWER".equalsIgnoreCase(role)) {
            return "redirect:/dashboard";
        }
        try {
            Snippet snippet = snippetManager.getSnippetById(id);
            model.addAttribute("snippet", snippet);
            return "review_form";
        } catch (Exception e) {
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/saveReview")
    public String saveReview(
            @RequestParam("id") Long id,
            @RequestParam("status") String status,
            @RequestParam("marks") String marks,
            @RequestParam(value = "reviewerComments", required = false) String reviewerComments,
            HttpSession session) {

        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }
        String role = (String) session.getAttribute("role");
        if (!"REVIEWER".equalsIgnoreCase(role)) {
            return "redirect:/dashboard";
        }
        try {
            int m = Integer.parseInt(marks.trim());
            Snippet s = snippetManager.getSnippetById(id);
            s.setStatus(status);
            s.setMarks(m);
            s.setReviewerComments(reviewerComments);
            snippetManager.saveSnippet(s);
        } catch (Exception ignored) {
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/delete/{id}")
    public String deleteSnippet(@PathVariable("id") Long id, HttpSession session) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }
        try {
            if (snippetManager.exists(id)) {
                Snippet snippet = snippetManager.getSnippetById(id);
                String role = (String) session.getAttribute("role");
                String username = (String) session.getAttribute("username");
                if ("REVIEWER".equalsIgnoreCase(role)
                        || (snippet.getUsn() != null && snippet.getUsn().equalsIgnoreCase(username))) {
                    snippetManager.deleteSnippet(id);
                }
            }
        } catch (Exception ignored) {
        }
        return "redirect:/dashboard";
    }
}
