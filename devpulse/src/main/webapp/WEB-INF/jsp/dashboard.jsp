<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, com.example.demo.entity.Snippet" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>DevPulse - Dashboard</title>
    <!-- Bootstrap CSS -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-ENjdO4Dr2bkBIFxQpeoA6W2Rk5i1NQ9V+8WfLjD3e9e8I4iZs4C5p5c8rKqV4g3Y" crossorigin="anonymous">
<style>
  body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f8f9fa; padding: 20px; }
  .header { background: #2c3e50; color: #fff; padding: 15px 25px; border-radius: 6px; display: flex; justify-content: space-between; align-items: center; }
  .user-info { font-size: 14px; }
  .nav-links a { color: #ecf0f1; text-decoration: none; margin-left: 15px; font-weight: 600; padding: 6px 12px; border-radius: 4px; background: rgba(255,255,255,0.15); }
  .nav-links a:hover { background: rgba(255,255,255,0.3); }
  .content { margin-top: 25px; }
  .snippet-card { background: #fff; border-radius: 6px; padding: 20px; margin-bottom: 20px; box-shadow: 0 2px 6px rgba(0,0,0,0.08); }
  .snippet-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #eee; padding-bottom: 10px; margin-bottom: 10px; }
  .badge { padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: bold; color: #fff; }
  .badge-pending { background: #f39c12; }
  .badge-approved { background: #27ae60; }
  .badge-revision { background: #e74c3c; }
  .badge-lang { background: #34495e; }
  pre { background: #2d3436; color: #dfe6e9; padding: 15px; border-radius: 4px; overflow-x: auto; font-family: 'Consolas', monospace; font-size: 13px; }
  .actions { margin-top: 15px; display: flex; gap: 10px; }
  .btn-primary { background: #2980b9; color: #fff; }
  .btn-danger { background: #c0392b; color: #fff; }
  .review-box { background: #f1f2f6; padding: 10px; border-radius: 4px; margin-top: 10px; font-size: 13px; }
</style>
</head>
<body>
<%
    String username = (String) session.getAttribute("username");
    String role = (String) session.getAttribute("role");
    String fullName = (String) session.getAttribute("fullName");
    List<Snippet> list = (List<Snippet>) request.getAttribute("snippetlist");
%>
<div class="header">
    <div>
        <h2 style="margin: 0;">DevPulse Dashboard</h2>
        <div class="user-info">Logged in as: <strong><%= fullName != null ? fullName : username %></strong> (<%= role %>)</div>
    </div>
    <div class="nav-links">
        <a href="${pageContext.request.contextPath}/SessionServlet?action=info">Session Info (Exp 1a)</a>
        <% if ("STUDENT".equalsIgnoreCase(role)) { %>
            <a href="${pageContext.request.contextPath}/new" style="background: #27ae60;">+ Submit Snippet (Exp 2a)</a>
        <% } %>
        <a href="${pageContext.request.contextPath}/SessionServlet?action=logout" style="background: #c0392b;">Logout</a>
    </div>
</div>

<div class="content">
    <% if (list == null || list.isEmpty()) { %>
        <div class="snippet-card" style="text-align: center; color: #7f8c8d;">
            <h3>No code snippets submitted yet.</h3>
            <% if ("STUDENT".equalsIgnoreCase(role)) { %>
                <p><a href="${pageContext.request.contextPath}/new" class="btn btn-primary">Submit First Lab Snippet</a></p>
            <% } %>
        </div>
    <% } else { %>
        <% for (Snippet s : list) { %>
            <div class="snippet-card">
                <div class="snippet-header">
                    <div>
                        <h3 style="margin: 0 0 5px 0;"><%= s.getTitle() %></h3>
                        <small style="color: #7f8c8d;">
                            Exp #<%= s.getLabExpNo() %> &bull; Student: <%= s.getStudentName() %> (<%= s.getUsn() %>) &bull; <%= s.getEmail() %>
                        </small>
                    </div>
                    <div>
                        <span class="badge badge-lang"><%= s.getLanguage() %></span>
                        <% if ("APPROVED".equalsIgnoreCase(s.getStatus())) { %>
                            <span class="badge badge-approved">APPROVED</span>
                        <% } else if ("NEEDS_REVISION".equalsIgnoreCase(s.getStatus())) { %>
                            <span class="badge badge-revision">NEEDS REVISION</span>
                        <% } else { %>
                            <span class="badge badge-pending">PENDING</span>
                        <% } %>
                    </div>
                </div>

                <pre><code><%= s.getSanitizedCode() != null ? s.getSanitizedCode() : "" %></code></pre>

                <% if (s.getReviewerComments() != null || s.getMarks() > 0) { %>
                    <div class="review-box">
                        <strong>Reviewer Marks:</strong> <%= s.getMarks() %>/10 &bull;
                        <strong>Feedback:</strong> <%= s.getReviewerComments() != null ? s.getReviewerComments() : "None" %>
                    </div>
                <% } %>

                <div class="actions">
                    <% if ("REVIEWER".equalsIgnoreCase(role)) { %>
                        <a href="${pageContext.request.contextPath}/review/<%= s.getId() %>" class="btn btn-primary">Review & Grade (Exp 3)</a>
                        <a href="${pageContext.request.contextPath}/delete/<%= s.getId() %>" class="btn btn-danger" onclick="return confirm('Delete snippet #<%= s.getId() %>?');">Delete</a>
                    <% } else if (s.getUsn() != null && s.getUsn().equalsIgnoreCase(username)) { %>
                        <a href="${pageContext.request.contextPath}/delete/<%= s.getId() %>" class="btn btn-danger" onclick="return confirm('Delete your snippet?');">Delete</a>
                    <% } %>
                </div>
            </div>
        <% } %>
    <% } %>
</div>
</body>
</html>
