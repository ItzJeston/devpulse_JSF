-- DevPulse Database Schema Definition
-- Cross-compatible with MySQL 8.0+ and H2 2.x (MODE=MySQL)
-- Supports idempotent execution (CREATE TABLE IF NOT EXISTS)

-- ============================================================================
-- 1. Users Table (Raw JDBC Authentication & RBAC - Experiment 3 Pattern)
-- ============================================================================
CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    full_name VARCHAR(100)
);

-- ============================================================================
-- 2. Snippet Table (Spring Data JPA Persistence - Experiment 4 & 5 Pattern)
-- ============================================================================
CREATE TABLE IF NOT EXISTS snippet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usn VARCHAR(20),
    student_name VARCHAR(100),
    email VARCHAR(100),
    lab_exp_no INT,
    language VARCHAR(30),
    title VARCHAR(100),
    raw_code TEXT,
    sanitized_code TEXT,
    status VARCHAR(30),
    reviewer_comments VARCHAR(500),
    marks INT
);
