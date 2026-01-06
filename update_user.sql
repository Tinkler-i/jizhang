DELETE FROM user WHERE username='admin';
INSERT INTO user (username, password, status) VALUES ('admin', '$2a$10$PEeFKpB9LdJddx5LJ8Tz5OduqWvyNKHWnqmDMl9F6fL2HvO3q0h4C', 1);
SELECT * FROM user WHERE username='admin';
