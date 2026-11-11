<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>DevPulse - Submission Validation Result</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f8f9fa; margin: 0; padding: 20px; display: flex; justify-content: center; }
        .result-container { background: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); width: 720px; }
        h2 { color: #2c3e50; margin-top: 0; }
        .alert-error { background: #fde8e8; border-left: 4px solid #e74c3c; padding: 15px; border-radius: 4px; margin-bottom: 20px; }
        .alert-success { background: #e8f8f5; border-left: 4px solid #27ae60; padding: 15px; border-radius: 4px; margin-bottom: 20px; }
        table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
        th, td { text-align: left; padding: 8px 12px; border-bottom: 1px solid #ddd; font-size: 14px; }
        th { background: #f1f2f6; width: 30%; color: #34495e; }
        pre { background: #2d3436; color: #dfe6e9; padding: 15px; border-radius: 4px; overflow-x: auto; font-family: 'Consolas', monospace; font-size: 13px; }
        .btn { padding: 10px 20px; border-radius: 4px; font-size: 14px; font-weight: bold; border: none; cursor: pointer; text-decoration: none; display: inline-block; }
        .btn-confirm { background: #27ae60; color: #fff; }
        .btn-back { background: #95a5a6; color: #fff; }
    </style>
</head>
<body>
<%
    Boolean isValid = (Boolean) request.getAttribute("isValid");
    List<String> errors = (List<String>) request.getAttribute("errors");
    String sanitizedCode = (String) request.getAttribute("sanitizedCode");
    String rawCode = (String) request.getAttribute("rawCode");
    String usn = (String) request.getAttribute("usn");
    String studentName = (String) request.getAttribute("studentName");
    String email = (String) request.getAttribute("email");
    String labExpNo = (String) request.getAttribute("labExpNo");
    String language = (String) request.getAttribute("language");
    String title = (String) request.getAttribute("title");
%>
<div class="result-container">
    <h2>Server-Side Regex Validation & XSS Sanitization (Exp 2a & 2b)</h2>

    <% if (Boolean.TRUE.equals(isValid)) { %>
        <div class="alert-success">
            <strong>Validation Passed!</strong> All manual regex boundary rules passed cleanly. HTML and script entities have been safely sanitized.
        </div>

        <table>
            <tr><th>Student Details</th><td><%= studentName %> (<%= usn %>)</td></tr>
            <tr><th>Email</th><td><%= email != null ? email.toLowerCase() : "" %></td></tr>
            <tr><th>Lab Experiment</th><td>Exp #<%= labExpNo %> - <%= language %></td></tr>
            <tr><th>Snippet Title</th><td><%= title %></td></tr>
            <tr><th>XSS Sanitization</th><td><span style="color:#27ae60; font-weight:bold;">&lt; &gt; &amp; &quot; &#x27; converted to HTML entities</span></td></tr>
        </table>

        <h4>Sanitized Preview:</h4>
        <pre><code><%= sanitizedCode %></code></pre>

        <form action="${pageContext.request.contextPath}/saveValidated" method="POST" style="margin-top: 20px;">
            <input type="hidden" name="usn" value="<%= usn %>">
            <input type="hidden" name="studentName" value="<%= studentName %>">
            <input type="hidden" name="email" value="<%= email %>">
            <input type="hidden" name="labExpNo" value="<%= labExpNo %>">
            <input type="hidden" name="language" value="<%= language %>">
            <input type="hidden" name="title" value="<%= title %>">
            <input type="hidden" name="rawCode" value="<%= rawCode %>">
            <input type="hidden" name="sanitizedCode" value="<%= sanitizedCode %>">
            <button type="submit" class="btn btn-confirm">Confirm & Save Snippet to Database (Exp 4)</button>
            <a href="${pageContext.request.contextPath}/new" class="btn btn-back">Edit Form</a>
        </form>

    <% } else { %>
        <div class="alert-error">
            <h3 style="margin-top:0;">Validation Errors Encountered:</h3>
            <ul>
                <% if (errors != null) { for (String err : errors) { %>
                    <li><%= err %></li>
                <% } } %>
            </ul>
        </div>
        <p><a href="javascript:history.back()" class="btn btn-back">&laquo; Go Back and Correct Errors</a></p>
    <% } %>
</div>
</body>
</html>
