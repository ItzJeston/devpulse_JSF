<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>DevPulse - Submit Lab Snippet</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f8f9fa; margin: 0; padding: 20px; display: flex; justify-content: center; }
        .form-container { background: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); width: 680px; }
        h2 { color: #2c3e50; margin-top: 0; border-bottom: 2px solid #3498db; padding-bottom: 10px; }
        .form-row { display: flex; gap: 15px; margin-bottom: 15px; }
        .form-group { flex: 1; margin-bottom: 15px; }
        label { display: block; font-weight: 600; font-size: 13px; margin-bottom: 5px; color: #34495e; }
        input[type="text"], input[type="email"], select, textarea { width: 100%; box-sizing: border-box; padding: 8px 12px; border: 1px solid #ccc; border-radius: 4px; font-size: 14px; font-family: inherit; }
        textarea { font-family: 'Consolas', monospace; resize: vertical; height: 180px; }
        .buttons { display: flex; gap: 10px; margin-top: 20px; }
        button { padding: 10px 20px; border: none; border-radius: 4px; font-size: 14px; font-weight: bold; cursor: pointer; }
        .btn-submit { background: #27ae60; color: #fff; }
        .btn-cancel { background: #95a5a6; color: #fff; text-decoration: none; padding: 10px 20px; border-radius: 4px; font-size: 14px; display: inline-block; }
    </style>
</head>
<body>
<%
    String username = (String) session.getAttribute("username");
    String fullName = (String) session.getAttribute("fullName");
%>
<div class="form-container">
    <h2>Submit Code Snippet (Experiment 2a & 2b)</h2>
    <form action="${pageContext.request.contextPath}/submit" method="POST">
        <div class="form-row">
            <div class="form-group">
                <label for="usn">USN (Format: ^NNM\d{2}CS\d{3}$)</label>
                <input type="text" id="usn" name="usn" value="<%= username != null ? username : "" %>" required>
            </div>
            <div class="form-group">
                <label for="studentName">Student Name (Letters and spaces)</label>
                <input type="text" id="studentName" name="studentName" value="<%= fullName != null ? fullName : "" %>" required>
            </div>
        </div>
        <div class="form-row">
            <div class="form-group">
                <label for="email">Student Email</label>
                <input type="email" id="email" name="email" placeholder="name@nmamit.in" required>
            </div>
            <div class="form-group">
                <label for="labExpNo">Lab Experiment No. (1 to 10)</label>
                <input type="text" id="labExpNo" name="labExpNo" placeholder="1-10" required>
            </div>
            <div class="form-group">
                <label for="language">Programming Language</label>
                <select id="language" name="language" required>
                    <option value="Java">Java</option>
                    <option value="JSP">JSP</option>
                    <option value="SQL">SQL</option>
                    <option value="HTML">HTML</option>
                </select>
            </div>
        </div>
        <div class="form-group">
            <label for="title">Lab Title / Topic</label>
            <input type="text" id="title" name="title" placeholder="e.g. Session Management with Servlets" required>
        </div>
        <div class="form-group">
            <label for="rawCode">Source Code Snippet (HTML/Script tags will be sanitized via Exp 2b regex)</label>
            <textarea id="rawCode" name="rawCode" placeholder="Paste your lab experiment code snippet here..." required></textarea>
        </div>
        <div class="buttons">
            <button type="submit" class="btn-submit">Validate & Preview (Exp 2b)</button>
            <a href="${pageContext.request.contextPath}/dashboard" class="btn-cancel">Cancel</a>
        </div>
    </form>
</div>
</body>
</html>
