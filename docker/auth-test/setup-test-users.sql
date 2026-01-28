-- Initial security data (roles required for the application to function)
-- These must be created before WebAPI can properly handle authentication
INSERT INTO webapi.sec_role (id, name, system_role) VALUES (1, 'public', true) ON CONFLICT (id) DO NOTHING;
INSERT INTO webapi.sec_role (id, name, system_role) VALUES (2, 'admin', true) ON CONFLICT (id) DO NOTHING;
INSERT INTO webapi.sec_role (id, name, system_role) VALUES (1001, 'Atlas users', true) ON CONFLICT (id) DO NOTHING;
INSERT INTO webapi.sec_role (id, name, system_role) VALUES (1002, 'Moderator', true) ON CONFLICT (id) DO NOTHING;

-- Anonymous user (required for public endpoints)
INSERT INTO webapi.sec_user (id, login, name) VALUES (1, 'anonymous', 'anonymous') ON CONFLICT (id) DO NOTHING;
INSERT INTO webapi.sec_user_role (id, user_id, role_id, origin) VALUES (1, 1, 1, 'SYSTEM') ON CONFLICT (id) DO NOTHING;

-- Update sequences to avoid conflicts
SELECT setval('webapi.sec_role_sequence', GREATEST((SELECT COALESCE(MAX(id), 0) + 1 FROM webapi.sec_role), nextval('webapi.sec_role_sequence')));
SELECT setval('webapi.sec_user_sequence', GREATEST((SELECT COALESCE(MAX(id), 0) + 1 FROM webapi.sec_user), nextval('webapi.sec_user_sequence')));
SELECT setval('webapi.sec_user_role_sequence', GREATEST((SELECT COALESCE(MAX(id), 0) + 1 FROM webapi.sec_user_role), nextval('webapi.sec_user_role_sequence')));

-- Test users table for JDBC authentication
CREATE TABLE IF NOT EXISTS webapi.users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    firstname VARCHAR(100),
    middlename VARCHAR(100),
    lastname VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- testpass123 (bcrypt)
INSERT INTO webapi.users (email, password, firstname, lastname)
VALUES (
    'testuser@example.com',
    '$2a$10$XBta6lTOBvpIB2Lqa8kCj.da4LOsAgH01YpcQB9l2AU7ip.G1mzsu',
    'Test',
    'User'
) ON CONFLICT (email) DO NOTHING;

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

SELECT 'Test user created:' AS info, email, firstname FROM webapi.users WHERE email = 'testuser@example.com';
