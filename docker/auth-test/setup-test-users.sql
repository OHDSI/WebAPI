-- Initial security data (roles required for the application to function)
-- These must be created before WebAPI can properly handle authentication
INSERT INTO webapi.sec_role (id, name, system_role) VALUES (1, 'public', true) ON CONFLICT (id) DO NOTHING;
INSERT INTO webapi.sec_role (id, name, system_role) VALUES (2, 'admin', true) ON CONFLICT (id) DO NOTHING;
INSERT INTO webapi.sec_role (id, name, system_role) VALUES (1001, 'Atlas users', true) ON CONFLICT (id) DO NOTHING;
INSERT INTO webapi.sec_role (id, name, system_role) VALUES (1002, 'Moderator', true) ON CONFLICT (id) DO NOTHING;

-- Anonymous user (required for public endpoints)
INSERT INTO webapi.sec_user (id, login, name) VALUES (1, 'anonymous', 'anonymous') ON CONFLICT DO NOTHING;
INSERT INTO webapi.sec_user_role (id, user_id, role_id, origin) VALUES (1, 1, 1, 'SYSTEM') ON CONFLICT DO NOTHING;

-- Update sequences to avoid conflicts
SELECT setval('webapi.sec_role_sequence', GREATEST((SELECT COALESCE(MAX(id), 0) + 1 FROM webapi.sec_role), nextval('webapi.sec_role_sequence')));
SELECT setval('webapi.sec_user_sequence', GREATEST((SELECT COALESCE(MAX(id), 0) + 1 FROM webapi.sec_user), nextval('webapi.sec_user_sequence')));
SELECT setval('webapi.sec_user_role_sequence', GREATEST((SELECT COALESCE(MAX(id), 0) + 1 FROM webapi.sec_user_role), nextval('webapi.sec_user_role_sequence')));

-- Auth user table for database authentication (matches DatabaseUserDetailsService schema)
CREATE TABLE IF NOT EXISTS webapi.auth_user (
    login VARCHAR(255) PRIMARY KEY,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    middle_name VARCHAR(100),
    last_name VARCHAR(100),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP
);

-- testpass123 (bcrypt)
INSERT INTO webapi.auth_user (login, password_hash, first_name, last_name)
VALUES (
    'testuser@example.com',
    '{bcrypt}$2a$10$XBta6lTOBvpIB2Lqa8kCj.da4LOsAgH01YpcQB9l2AU7ip.G1mzsu',
    'Test',
    'User'
) ON CONFLICT (login) DO NOTHING;

INSERT INTO webapi.sec_role (id, name, system_role)
VALUES (10001, 'test-admin', true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO webapi.sec_user (id, login, name, origin)
VALUES (10001, 'testuser@example.com', 'Test User', 'SYSTEM')
ON CONFLICT (id) DO NOTHING;

INSERT INTO webapi.sec_user_role (id, user_id, role_id, origin)
VALUES (10001, 10001, 10001, 'SYSTEM')
ON CONFLICT (id) DO NOTHING;

INSERT INTO webapi.sec_permission (id, value)
SELECT 10001, 'source:get'
WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10001);

INSERT INTO webapi.sec_permission (id, value)
SELECT 10002, 'source:*:get'
WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10002);

INSERT INTO webapi.sec_role_permission (id, role_id, permission_id)
SELECT 10001, 10001, 10001
WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10001);

INSERT INTO webapi.sec_role_permission (id, role_id, permission_id)
SELECT 10002, 10001, 10002
WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10002);

SELECT 'Test user created:' AS info, login, first_name FROM webapi.auth_user WHERE login = 'testuser@example.com';
