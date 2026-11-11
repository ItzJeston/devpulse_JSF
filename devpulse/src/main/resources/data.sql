-- DevPulse Seed Data
-- Cross-compatible with MySQL 8.0+ and H2 2.x (MODE=MySQL)
-- Idempotent execution: INSERT IGNORE skips duplicate keys across restarts

-- Seed default team members (STUDENT role) and faculty reviewer (REVIEWER role)
INSERT IGNORE INTO users (username, password, role, full_name) VALUES ('NNM24CS107', 'pass107', 'STUDENT', 'Jeston Correa');
INSERT IGNORE INTO users (username, password, role, full_name) VALUES ('NNM24CS108', 'pass108', 'STUDENT', 'Jnanesh Karkera');
INSERT IGNORE INTO users (username, password, role, full_name) VALUES ('NNM24CS100', 'pass100', 'STUDENT', 'Ian Roniel Soans');
INSERT IGNORE INTO users (username, password, role, full_name) VALUES ('NNM24CS076', 'pass076', 'STUDENT', 'Dhanush U. Shetty');
INSERT IGNORE INTO users (username, password, role, full_name) VALUES ('admin', 'password', 'REVIEWER', 'Prof. Akhilraj V. Gadagkar');
