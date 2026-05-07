-- ID: random UUID, password: 'admin123'
-- Hash for 'admin123' using 10 rounds: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIvi

INSERT INTO users (id, email, password, is_locked, name, surname)
VALUES ('3f8b9e6c-4c2d-4f1a-b3e1-2c9d8a7f6e5b', 'admin@example.com', '$2a$10$tzFDeInXvKWjN/Dz6Gmab.vzBmklm74fX7y.i8YbLj2cYBPjmhKsS', false, 'System', 'Admin');

INSERT INTO user_roles (user_id, role)
VALUES ('3f8b9e6c-4c2d-4f1a-b3e1-2c9d8a7f6e5b', 'ROLE_ADMIN');
