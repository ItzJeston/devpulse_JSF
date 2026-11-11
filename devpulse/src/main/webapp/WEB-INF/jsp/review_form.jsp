<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.demo.entity.Snippet" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>DevPulse - Review & Grade Snippet</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f8f9fa; margin: 0; padding: 20px; display: flex; justify-content: center; }
        .review-container { background: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); width: 720px; }
        h2 { color: #2c3e50; margin-top: 0; border-bottom: 2px solid #2980b9; padding-bottom: 10px; }
        .meta-table { width: 100%; border-collapse: collapse; margin-bottom: 15px; font-size: 14px; }
        .meta-table th, .meta-table td { padding: 8px 12px; border-bottom: 1px solid #eee; text-align: left; }
        .meta-table th { width: 25%; background: #f8f9fa; color: #555; }
        pre { background: #2d3436; color: #dfe6e9; padding: 15px; border-radius: 4px; overflow-x: auto; font-family: 'Consolas', monospace; font-size: 13px; }
        .form-group { margin-bottom: 15px; }
        label { display: block; font-weight: 600; font-size: 13px; margin-bottom: 5px; color: #34495e; }
        input[type="number"], select, textarea { width: 100%; box-sizing: border-box; padding: 8px 12px; border: 1px solid #ccc; border-radius: 4px; font-size: 14px; }
        textarea { height: 90px; }
        .btn-save { background: #27ae60; color: #fff; padding: 10px 20px; border: none; border-radius: 4px; font-size: 14px; font-weight: bold; cursor: pointer; }
        .btn-cancel { background: #95a5a6; color: #fff; padding: 10px 20px; border-radius: 4px; font-size: 14px; text-decoration: none; display: inline-block; margin-left: 10px; }
    </style>
</head>
<body>
<%
    Snippet s = (Snippet) request.getAttribute("snippet");
%>
<div class="review-container">
    <h2>Review & Grade Lab Submission (Experiment 3 & 4)</h2>
    <% if (s != null) { %>
        <table class="meta-table">
            <tr><th>Snippet ID</th><td>#<%= s.getId() %></td></tr>
            <tr><th>Student</th><td><%= s.getStudentName() %> (<%= s.getUsn() %>)</td></tr>
            <tr><th>Email</th><td><%= s.getEmail() %></td></tr>
            <tr><th>Experiment</th><td>Exp #<%= s.getLabExpNo() %> (<%= s.getLanguage() %>) - <%= s.getTitle() %></td></tr>
            <tr><th>Current Status</th><td><strong><%= s.getStatus() %></strong></td></tr>
        </table>

        <h4>Sanitized Submitted Code:</h4>
        <pre><code><%= s.getSanitizedCode() != null ? s.getSanitizedCode() : "" %></code></pre>

        <form action="${pageContext.request.contextPath}/saveReview" method="POST" style="margin-top: 20px;">
            <input type="hidden" name="id" value="<%= s.getId() %>">
            <div class="form-group">
                <label for="status">Review Decision</label>
                <select id="status" name="status" required>
                    <option value="APPROVED" <%= "APPROVED".equalsIgnoreCase(s.getStatus()) ? "selected" : "" %>>APPROVED</option>
                    <option value="NEEDS_REVISION" <%= "NEEDS_REVISION".equalsIgnoreCase(s.getStatus()) ? "selected" : "" %>>NEEDS REVISION</option>
                    <option value="PENDING" <%= "PENDING".equalsIgnoreCase(s.getStatus()) ? "selected" : "" %>>PENDING</option>
                </select>
            </div>
            <div class="form-group">
                <label for="marks">Lab Marks Awarded (0 to 10)</label>
                <input type="number" id="marks" name="marks" min="0" max="10" value="<%= s.getMarks() %>" required>
            </div>
            <div class="form-group">
                <label for="reviewerComments">Faculty Reviewer Comments & Feedback</label>
                <textarea id="reviewerComments" name="reviewerComments" placeholder="Enter constructive feedback, errors spotted, or code suggestions..."><%= s.getReviewerComments() != null ? s.getReviewerComments() : "" %></textarea>
            </div>
            <button type="submit" class="btn-save">Submit Grade & Feedback</button>
            <a href="${pageContext.request.contextPath}/dashboard" class="btn-cancel">Back to Dashboard</a>
        </form>
    <% } else { %>
        <p>Snippet not found.</p>
        <a href="${pageContext.request.contextPath}/dashboard" class="btn-cancel">Back to Dashboard</a>
    <% } %>
</div>
</body>
</html>
