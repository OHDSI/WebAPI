-- Integration Test Data Setup
-- This script is idempotent and can be run multiple times safely

-- Cleanup existing test artifacts (ignore errors for missing tables on fresh runs)
DO $$ BEGIN DELETE FROM webapi.cohort_definition WHERE id >= 10001 AND id < 20000; EXCEPTION WHEN undefined_table THEN NULL; END $$;
DO $$ BEGIN DELETE FROM webapi.concept_set WHERE concept_set_id >= 10001 AND concept_set_id < 20000; EXCEPTION WHEN undefined_table THEN NULL; END $$;
DELETE FROM webapi.sec_user_role WHERE id >= 10001 AND id < 20000;
DELETE FROM webapi.sec_role_permission WHERE id >= 10001 AND id < 20000;
DELETE FROM webapi.sec_permission WHERE id >= 10001 AND id < 20000;
DELETE FROM webapi.sec_role WHERE id >= 10001 AND id < 20000;
DELETE FROM webapi.sec_user WHERE id >= 10001 AND id < 20000;
DELETE FROM webapi.source_daimon WHERE source_id = 1;
DELETE FROM webapi.source WHERE source_id = 1;

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

-- Test users (passwords: testpass123, adminpass123)
INSERT INTO webapi.auth_user (login, password_hash, first_name, last_name) VALUES
    ('testuser@example.com', '{bcrypt}$2a$10$XBta6lTOBvpIB2Lqa8kCj.da4LOsAgH01YpcQB9l2AU7ip.G1mzsu', 'Test', 'User'),
    ('admin@example.com', '{bcrypt}$2a$10$kDpJMpJqX5GDLMJqmWr1/.9v0x.yWVYGaXMOVdXPYMTqXhZpqcFfC', 'Admin', 'User')
ON CONFLICT (login) DO UPDATE SET password_hash = EXCLUDED.password_hash;

-- Security roles (IDs 10001-10010 reserved for test data)
INSERT INTO webapi.sec_role (id, name, system_role) VALUES
    (10001, 'test-admin', true),
    (10002, 'test-user', false),
    (10003, 'testuser@example.com', false),
    (10004, 'admin@example.com', false)
ON CONFLICT (id) DO NOTHING;

-- Security users (must match login in users table)
INSERT INTO webapi.sec_user (id, login, name, origin) VALUES
    (10001, 'testuser@example.com', 'Test User', 'SYSTEM'),
    (10002, 'admin@example.com', 'Admin User', 'SYSTEM')
ON CONFLICT (id) DO NOTHING;

-- User-role assignments
INSERT INTO webapi.sec_user_role (id, user_id, role_id, origin) VALUES
    (10001, 10001, 10001, 'SYSTEM'),
    (10002, 10002, 10001, 'SYSTEM'),
    (10003, 10001, 10003, 'SYSTEM'),
    (10004, 10002, 10004, 'SYSTEM')
ON CONFLICT (id) DO NOTHING;

-- Grant all existing permissions to test-admin role (role_id=10001)
-- Permissions are created by Flyway migrations, so we reference them by value
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id)
SELECT nextval('webapi.sec_role_permission_sequence'), 10001, p.id FROM webapi.sec_permission p
WHERE NOT EXISTS (
    SELECT 1 FROM webapi.sec_role_permission rp
    WHERE rp.role_id = 10001 AND rp.permission_id = p.id
);

-- CDM data source (broadsea-atlasdb default password: mypass)
INSERT INTO webapi.source (source_id, source_name, source_key, source_connection, source_dialect, username, password)
VALUES (1, 'Demo CDM', 'DEMO_CDM', 'jdbc:postgresql://cdm-db:5432/postgres', 'postgresql', 'postgres', 'mypass')
ON CONFLICT (source_id) DO NOTHING;

-- Daimons: 0=CDM, 1=Vocabulary, 2=Results, 5=Temp
INSERT INTO webapi.source_daimon (source_daimon_id, source_id, daimon_type, table_qualifier, priority) VALUES
    (1, 1, 0, 'demo_cdm', 1),
    (2, 1, 1, 'demo_cdm', 1),
    (3, 1, 2, 'demo_cdm_results', 1),
    (4, 1, 5, 'demo_cdm_results', 0)
ON CONFLICT (source_daimon_id) DO NOTHING;

-- Source role for DEMO_CDM
INSERT INTO webapi.sec_role (id, name, system_role) VALUES (10010, 'Source user (DEMO_CDM)', true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO webapi.sec_user_role (id, user_id, role_id, origin) VALUES
    (10010, 10001, 10010, 'SYSTEM'),
    (10011, 10002, 10010, 'SYSTEM')
ON CONFLICT (id) DO NOTHING;

SELECT 'Test data setup completed' AS status;
