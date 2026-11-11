<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>DevPulse - Login</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f0f2f5; display: flex; align-items: center; justify-content: center; height: 100vh; margin: 0; }
        .card { background: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); width: 360px; }
        h2 { text-align: center; color: #2c3e50; margin-top: 0; }
        .subtitle { text-align: center; color: #7f8c8d; font-size: 13px; margin-bottom: 20px; }
        .alert-error { background: #fde8e8; color: #c0392b; padding: 10px; border-radius: 4px; margin-bottom: 15px; font-size: 13px; text-align: center; }
        .alert-success { background: #e8f8f5; color: #27ae60; padding: 10px; border-radius: 4px; margin-bottom: 15px; font-size: 13px; text-align: center; }
        .form-group { margin-bottom: 15px; }
        label { display: block; margin-bottom: 5px; font-weight: 600; font-size: 13px; color: #34495e; }
        input[type="text"], input[type="password"] { width: 100%; box-sizing: border-box; padding: 10px; border: 1px solid #ccc; border-radius: 4px; font-size: 14px; }
        button { width: 100%; padding: 10px; background: #3498db; color: #fff; border: none; border-radius: 4px; font-size: 15px; font-weight: bold; cursor: pointer; transition: background 0.2s; }
        button:hover { background: #2980b9; }
        .hint { margin-top: 20px; background: #eef2f7; padding: 10px; border-radius: 4px; font-size: 11px; color: #555; }
    </style>
</head>
<body>
<div class="card">
    <h2>DevPulse</h2>
    <div class="subtitle">Java & Spring Framework Lab (CS3603-1)</div>

    <% if (request.getAttribute("error") != null || "true".equals(request.getParameter("error"))) { %>
        <div class="alert-error">Invalid USN/Username or Password. Please try again.</div>
    <% } %>

    <% if (request.getAttribute("logout") != null || "true".equals(request.getParameter("logout"))) { %>
        <div class="alert-success">You have been logged out successfully.</div>
    <% } %>

    <form action="${pageContext.request.contextPath}/SessionServlet" method="POST">
        <div class="form-group">
            <label for="username">USN / Username</label>
            <input type="text" id="username" name="username" placeholder="e.g. NNM24CS107 or admin" required autofocus>
        </div>
        <div class="form-group">
            <label for="password">Password</label>
            <input type="password" id="password" name="password" placeholder="e.g. password123 or admin123" required>
        </div>
        <button type="submit">Sign In</button>
    </form>

    <div class="hint">
        <strong>Default Test Credentials:</strong><br>
        • Student: <code>NNM24CS107</code> / <code>password123</code><br>
        • Reviewer: <code>admin</code> / <code>admin123</code>
    </div>
</div>
</body>
</html>
