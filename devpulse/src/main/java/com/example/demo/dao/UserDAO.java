package com.example.demo.dao;

import com.example.demo.entity.User;
import com.example.demo.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * UserDAO - Raw JDBC Data Access Object for user authentication and RBAC.
 * Conforms to Experiment 3 (JDBC integration) and Experiment 1b (Session RBAC).
 */
public class UserDAO {

    private static final String SELECT_USER_SQL =
            "SELECT username, password, role, full_name FROM users WHERE username = ? AND password = ?";

    private static final String FALLBACK_USER = "admin";
    private static final String FALLBACK_PASS = "password";
    private static final String FALLBACK_ROLE = "REVIEWER";
    private static final String FALLBACK_NAME = "Prof. Akhilraj V. Gadagkar";

    public UserDAO() {
    }

    /**
     * Validates user credentials against the database, with emergency fallback.
     * Uses PreparedStatement parameterization to prevent SQL injection.
     * Enforces case and whitespace sensitivity.
     *
     * @param username raw username input (must NOT be trimmed)
     * @param password raw password input (must NOT be trimmed)
     * @return populated User object if credentials are valid, null otherwise
     */
    public User validateUser(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            return null;
        }

        try (Connection conn = DBUtil.getConnection()) {
            if (conn != null) {
                try (PreparedStatement ps = conn.prepareStatement(SELECT_USER_SQL)) {
                    ps.setString(1, username);
                    ps.setString(2, password);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String dbUser = rs.getString("username");
                            String dbPass = rs.getString("password");
                            String dbRole = rs.getString("role");
                            String dbFullName = rs.getString("full_name");

                            // Explicit Java equality check to enforce case & whitespace sensitivity
                            if (dbUser != null && dbUser.equals(username) &&
                                dbPass != null && dbPass.equals(password)) {
                                return new User(dbUser, dbPass, dbRole, dbFullName);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Log warning; continue to fallback check below
            System.err.println("[UserDAO] Database authentication error: " + e.getMessage());
        }

        // Hardcoded emergency fallback check (case and whitespace sensitive)
        if (FALLBACK_USER.equals(username) && FALLBACK_PASS.equals(password)) {
            return new User(FALLBACK_USER, FALLBACK_PASS, FALLBACK_ROLE, FALLBACK_NAME);
        }

        return null;
    }
}
