package com.example.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DevPulseApplicationTests - Application Context and Environment Bootstrap Test.
 *
 * Verifies:
 * 1. ApplicationContext boots up without errors under the H2 "test" profile.
 * 2. DataSource bean is present and connects successfully to in-memory H2 database.
 * 3. Spring MVC JSP View Resolver properties (prefix and suffix) are correctly configured.
 * 4. Schema and seed data are correctly executed (users table contains initial 5 accounts).
 */
@SpringBootTest
@ActiveProfiles("test")
class DevPulseApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Environment environment;

    @Test
    @DisplayName("Verify Spring application context loads successfully")
    void contextLoads() {
        assertNotNull(applicationContext, "ApplicationContext must not be null");
        assertNotNull(dataSource, "DataSource bean must be present in ApplicationContext");
    }

    @Test
    @DisplayName("Verify DataSource connection is active and connects to H2 in-memory test database")
    void testDataSourceConnection() throws Exception {
        assertNotNull(dataSource, "DataSource bean should be configured and injected");
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "Active database connection should be obtained");
            assertTrue(connection.isValid(2), "Database connection should be valid");
            String dbProductName = connection.getMetaData().getDatabaseProductName();
            assertNotNull(dbProductName, "Database product name should be available");
            assertTrue(dbProductName.toLowerCase().contains("h2"),
                    "Test database product should be H2, but was: " + dbProductName);
        }
    }

    @Test
    @DisplayName("Verify JSP view resolver properties are properly loaded")
    void testJspViewResolverProperties() {
        String prefix = environment.getProperty("spring.mvc.view.prefix");
        String suffix = environment.getProperty("spring.mvc.view.suffix");
        assertEquals("/WEB-INF/jsp/", prefix, "JSP view prefix should match /WEB-INF/jsp/");
        assertEquals(".jsp", suffix, "JSP view suffix should match .jsp");
    }

    @Test
    @DisplayName("Verify schema.sql and data.sql successfully created and seeded the users table")
    void testSchemaAndDataInitialization() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM users")) {
            assertTrue(resultSet.next(), "Query should return at least one row");
            int userCount = resultSet.getInt(1);
            assertEquals(5, userCount, "Seed data should have created exactly 5 users");
        }
    }
}
