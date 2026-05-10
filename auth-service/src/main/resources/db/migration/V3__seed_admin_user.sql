-- V3__seed_admin_user.sql
-- Bootstraps the initial system admin account.
-- Password: 'admin123' — BCrypt hash (10 rounds).

INSERT INTO users (email, password, is_locked, name, surname, created_at, updated_at)
VALUES ('admin@example.com', '$2a$10$tzFDeInXvKWjN/Dz6Gmab.vzBmklm74fX7y.i8YbLj2cYBPjmhKsS', false, 'System', 'Admin', NOW(), NOW());

INSERT INTO user_roles (user_id, role)
VALUES (LAST_INSERT_ID(), 'ROLE_ADMIN');
