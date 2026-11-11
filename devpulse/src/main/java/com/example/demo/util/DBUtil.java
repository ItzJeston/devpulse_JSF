package com.example.demo.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DBUtil - Raw JDBC Connection Helper with automatic H2 in-memory fallback.
 * Conforms to Experiment 3 (JDBC integration) and test profile isolation.
 */
public class DBUtil {

    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/student?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=1000";
    private static final String MYSQL_USER = "student";
    private static final String MYSQL_PASS = "student";

    private static final String H2_URL = "jdbc:h2:mem:student;DB_CLOSE_DELAY=-1;MODE=MySQL";
    private static final String H2_USER = "sa";
    private static final String H2_PASS = "";

    private static volatile boolean mysqlFailed = false;
    private static volatile boolean h2Initialized = false;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {
        }
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException ignored) {
        }
    }

    /**
     * Determines whether the current execution thread is running within a test context.
     */
    public static boolean isTestContext() {
        String profile = System.getProperty("spring.profiles.active");
        if (profile != null && profile.contains("test")) {
            return true;
        }
        if ("true".equalsIgnoreCase(System.getProperty("devpulse.use.h2"))) {
            return true;
        }
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            String cls = ste.getClassName();
            if (cls.startsWith("org.junit.") || cls.startsWith("org.springframework.test.")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Obtains an active JDBC database connection.
     * In test environments or when MySQL is unreachable, seamlessly connects to H2 in-memory database.
     */
    public static Connection getConnection() throws SQLException {
        if (isTestContext() || mysqlFailed) {
            return getH2Connection();
        }

        try {
            return DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASS);
        } catch (SQLException e) {
            mysqlFailed = true;
            System.err.println("[DBUtil] MySQL connection failed. Falling back to H2 in-memory: " + e.getMessage());
            return getH2Connection();
        }
    }

    private static Connection getH2Connection() throws SQLException {
        Connection conn = DriverManager.getConnection(H2_URL, H2_USER, H2_PASS);
        ensureH2Schema(conn);
        return conn;
    }

    /**
     * Lazily and idempotently ensures users table and default seed data are present in H2.
     */
    private static synchronized void ensureH2Schema(Connection conn) {
        if (h2Initialized) {
            return;
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "username VARCHAR(50) PRIMARY KEY, " +
                    "password VARCHAR(50) NOT NULL, " +
                    "role VARCHAR(20) NOT NULL, " +
                    "full_name VARCHAR(100))");

            stmt.execute("INSERT IGNORE INTO users (username, password, role, full_name) VALUES " +
                    "('NNM24CS107', 'pass107', 'STUDENT', 'Jeston Correa'), " +
                    "('NNM24CS108', 'pass108', 'STUDENT', 'Jnanesh Karkera'), " +
                    "('NNM24CS100', 'pass100', 'STUDENT', 'Ian Roniel Soans'), " +
                    "('NNM24CS076', 'pass076', 'STUDENT', 'Dhanush U. Shetty'), " +
                    "('admin', 'password', 'REVIEWER', 'Prof. Akhilraj V. Gadagkar')");

            h2Initialized = true;
        } catch (SQLException e) {
            // Already initialized or concurrent execution
        }
    }

    public static void close(AutoCloseable... closeables) {
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
