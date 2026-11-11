package com.example.demo.servlet;

import com.example.demo.dao.UserDAO;
import com.example.demo.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * SessionServlet - Manual HTTP Session Lifecycle & Authentication Servlet.
 * Demonstrates Experiments 1a, 1b, and 3:
 * - Session-based authentication & role-based attribute management
 * - Session fixation defense (invalidating existing session upon login)
 * - Session diagnostics inspection (/SessionServlet?action=info)
 * - Session invalidation and logout (/SessionServlet?action=logout)
 */
@WebServlet("/SessionServlet")
public class SessionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private UserDAO userDAO;

    public SessionServlet() {
        super();
        this.userDAO = new UserDAO();
    }

    public SessionServlet(UserDAO userDAO) {
        super();
        this.userDAO = userDAO;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        if (this.userDAO == null) {
            this.userDAO = new UserDAO();
        }
    }

    /**
     * Handles authentication form submissions:
     * - Parameter parsing & trimming validation
     * - UserDAO credential verification
     * - Session fixation protection (invalidating old session)
     * - Attribute population ("username", "role", "fullName" as Strings)
     * - Redirection to /dashboard on success or /login?error=true on failure
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Validate parameter presence and non-whitespace
        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            redirect(request, response, "/login?error=true");
            return;
        }

        // Authenticate via UserDAO (pass raw username to preserve whitespace-sensitivity)
        User user = userDAO.validateUser(username, password);

        if (user != null) {
            // Defense against Session Fixation: invalidate existing session if present
            HttpSession existingSession = request.getSession(false);
            if (existingSession != null) {
                try {
                    existingSession.invalidate();
                } catch (IllegalStateException ignored) {
                    // Session was already invalidated
                }
            }

            // Establish fresh authenticated session
            HttpSession session = request.getSession(true);
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());
            session.setAttribute("fullName", user.getFullName());

            redirect(request, response, "/dashboard");
        } else {
            // Authentication failed
            redirect(request, response, "/login?error=true");
        }
    }

    /**
     * Handles session lifecycle actions:
     * - action=logout: invalidates session and redirects to /login
     * - action=info: renders HTML session diagnostics (id, creation, access, user, role)
     * - default: redirects to /dashboard if authenticated, else /login
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("logout".equalsIgnoreCase(action)) {
            handleLogout(request, response);
        } else if ("info".equalsIgnoreCase(action)) {
            handleInfo(request, response);
        } else {
            handleDefault(request, response);
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            try {
                session.invalidate();
            } catch (IllegalStateException ignored) {
                // Ignore if session was already invalidated
            }
        }
        redirect(request, response, "/login");
    }

    private void handleInfo(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        String cp = getContextPath(request);

        if (session == null) {
            out.println("<!DOCTYPE html>");
            out.println("<html lang=\"en\">");
            out.println("<head><title>Session Diagnostics</title></head>");
            out.println("<body>");
            out.println("<h2>Session Diagnostics</h2>");
            out.println("<p>No active session found. Please <a href=\"" + cp + "/login\">login</a>.</p>");
            out.println("</body>");
            out.println("</html>");
            return;
        }

        String sessionId = session.getId();
        long creationTime = 0;
        long lastAccessedTime = 0;
        try {
            creationTime = session.getCreationTime();
            lastAccessedTime = session.getLastAccessedTime();
        } catch (IllegalStateException ignored) {
            // Guard against already invalidated session
        }

        Date creationDate = new Date(creationTime);
        Date lastAccessedDate = new Date(lastAccessedTime);

        Object usernameObj = null;
        Object roleObj = null;
        Object fullNameObj = null;
        try {
            usernameObj = session.getAttribute("username");
            roleObj = session.getAttribute("role");
            fullNameObj = session.getAttribute("fullName");
        } catch (IllegalStateException ignored) {
        }

        String username = usernameObj != null ? usernameObj.toString() : "Anonymous / Not Logged In";
        String role = roleObj != null ? roleObj.toString() : "N/A";
        String fullName = fullNameObj != null ? fullNameObj.toString() : "N/A";

        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"en\">");
        out.println("<head><title>Session Diagnostics</title></head>");
        out.println("<body>");
        out.println("<h2>Session Diagnostics</h2>");
        out.println("<ul>");
        out.println("<li><strong>Session ID:</strong> " + sessionId + "</li>");
        out.println("<li><strong>Username:</strong> " + username + "</li>");
        out.println("<li><strong>Role:</strong> " + role + "</li>");
        out.println("<li><strong>Full Name:</strong> " + fullName + "</li>");
        out.println("<li><strong>Creation Time:</strong> " + creationDate + " (" + creationTime + ")</li>");
        out.println("<li><strong>Last Accessed Time:</strong> " + lastAccessedDate + " (" + lastAccessedTime + ")</li>");
        out.println("</ul>");
        out.println("<p><a href=\"" + cp + "/dashboard\">Dashboard</a></p>");
        out.println("<p><a href=\"" + (cp.isEmpty() ? "" : cp + "/") + "SessionServlet?action=logout\">Logout</a></p>");
        out.println("</body>");
        out.println("</html>");
    }

    private void handleDefault(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("username") != null) {
            redirect(request, response, "/dashboard");
        } else {
            redirect(request, response, "/login");
        }
    }

    private void redirect(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException {
        String cp = getContextPath(request);
        response.sendRedirect(cp + path);
    }

    private String getContextPath(HttpServletRequest request) {
        String cp = request.getContextPath();
        return (cp == null) ? "" : cp;
    }
}
