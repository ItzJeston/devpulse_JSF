<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>DevPulse - Login</title>
    <!-- Bootstrap CSS -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-ENjdO4Dr2bkBIFxQpeoA6W2Rk5i1NQ9V+8WfLjD3e9e8I4iZs4C5p5c8rKqV4g3Y" crossorigin="anonymous">
<style>
  body { background: #f0f2f5; }
  .login-card { max-width: 380px; margin: auto; margin-top: 10vh; }
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
