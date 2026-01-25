-- Integration Test Data Setup

CREATE TABLE IF NOT EXISTS webapi.users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    firstname VARCHAR(100),
    middlename VARCHAR(100),
    lastname VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Test users (passwords: testpass123, adminpass123)
INSERT INTO webapi.users (email, password, firstname, lastname) VALUES
    ('testuser@example.com', '$2a$10$XBta6lTOBvpIB2Lqa8kCj.da4LOsAgH01YpcQB9l2AU7ip.G1mzsu', 'Test', 'User'),
    ('admin@example.com', '$2a$10$kDpJMpJqX5GDLMJqmWr1/.9v0x.yWVYGaXMOVdXPYMTqXhZpqcFfC', 'Admin', 'User')
ON CONFLICT (email) DO NOTHING;

-- Security roles
INSERT INTO webapi.sec_role (id, name, system_role) VALUES
    (10001, 'test-admin', true),
    (10002, 'test-user', false),
    (10003, 'testuser@example.com', false),  -- personal role for entity creation
    (10004, 'admin@example.com', false)
ON CONFLICT (id) DO NOTHING;

-- Security users
INSERT INTO webapi.sec_user (id, login, name, origin) VALUES
    (10001, 'testuser@example.com', 'Test User', 'SYSTEM'),
    (10002, 'admin@example.com', 'Admin User', 'SYSTEM')
ON CONFLICT (id) DO NOTHING;

-- User-role assignments
INSERT INTO webapi.sec_user_role (id, user_id, role_id, origin) VALUES
    (10001, 10001, 10001, 'SYSTEM'),  -- testuser -> test-admin
    (10002, 10002, 10001, 'SYSTEM'),  -- admin -> test-admin
    (10003, 10001, 10003, 'SYSTEM'),  -- testuser -> personal role
    (10004, 10002, 10004, 'SYSTEM')   -- admin -> personal role
ON CONFLICT (id) DO NOTHING;

-- Permissions
INSERT INTO webapi.sec_permission (id, value) SELECT 10001, 'source:get' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10001);
INSERT INTO webapi.sec_permission (id, value) SELECT 10002, 'source:*:get' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10002);
INSERT INTO webapi.sec_permission (id, value) SELECT 10003, 'source:post' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10003);
INSERT INTO webapi.sec_permission (id, value) SELECT 10004, 'source:*:put' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10004);
INSERT INTO webapi.sec_permission (id, value) SELECT 10005, 'source:*:delete' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10005);

INSERT INTO webapi.sec_permission (id, value) SELECT 10010, 'cohortdefinition:get' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10010);
INSERT INTO webapi.sec_permission (id, value) SELECT 10011, 'cohortdefinition:*:get' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10011);
INSERT INTO webapi.sec_permission (id, value) SELECT 10012, 'cohortdefinition:post' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10012);
INSERT INTO webapi.sec_permission (id, value) SELECT 10013, 'cohortdefinition:*:put' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10013);
INSERT INTO webapi.sec_permission (id, value) SELECT 10014, 'cohortdefinition:*:delete' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10014);
INSERT INTO webapi.sec_permission (id, value) SELECT 10015, 'cohortdefinition:*:generate:*:get' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10015);
INSERT INTO webapi.sec_permission (id, value) SELECT 10016, 'cohortdefinition:*:info:get' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10016);
INSERT INTO webapi.sec_permission (id, value) SELECT 10017, 'cohortdefinition:*:report:*:get' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10017);
INSERT INTO webapi.sec_permission (id, value) SELECT 10020, 'vocabulary:*:search:post' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10020);
INSERT INTO webapi.sec_permission (id, value) SELECT 10021, 'vocabulary:*:concept:*:get' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10021);
INSERT INTO webapi.sec_permission (id, value) SELECT 10022, 'vocabulary:*:concept:*:related:get' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10022);
INSERT INTO webapi.sec_permission (id, value) SELECT 10023, 'vocabulary:lookup:identifiers:post' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10023);
INSERT INTO webapi.sec_permission (id, value) SELECT 10024, 'vocabulary:*:lookup:identifiers:post' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10024);
INSERT INTO webapi.sec_permission (id, value) SELECT 10025, 'vocabulary:*:lookup:identifiers:ancestors:post' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10025);
INSERT INTO webapi.sec_permission (id, value) SELECT 10030, 'conceptset:get' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10030);
INSERT INTO webapi.sec_permission (id, value) SELECT 10031, 'conceptset:*:get' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10031);
INSERT INTO webapi.sec_permission (id, value) SELECT 10032, 'conceptset:post' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10032);
INSERT INTO webapi.sec_permission (id, value) SELECT 10033, 'conceptset:*:put' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10033);
INSERT INTO webapi.sec_permission (id, value) SELECT 10034, 'conceptset:*:delete' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10034);
INSERT INTO webapi.sec_permission (id, value) SELECT 10040, 'cdmresults:*:get' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10040);
INSERT INTO webapi.sec_permission (id, value) SELECT 10041, 'cdmresults:*:*:get' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10041);
INSERT INTO webapi.sec_permission (id, value) SELECT 10042, 'cdmresults:*:conceptRecordCount:post' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10042);
INSERT INTO webapi.sec_permission (id, value) SELECT 10050, 'job:execution:get' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10050);
INSERT INTO webapi.sec_permission (id, value) SELECT 10051, 'job:*:get' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10051);
INSERT INTO webapi.sec_permission (id, value) SELECT 10060, 'info:get' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10060);
INSERT INTO webapi.sec_permission (id, value) SELECT 10070, 'user:me:get' WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_permission WHERE id = 10070);

-- Role-permission assignments (grant all to test-admin)
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10001, 10001, 10001 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10001);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10002, 10001, 10002 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10002);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10003, 10001, 10003 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10003);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10004, 10001, 10004 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10004);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10005, 10001, 10005 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10005);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10010, 10001, 10010 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10010);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10011, 10001, 10011 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10011);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10012, 10001, 10012 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10012);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10013, 10001, 10013 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10013);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10014, 10001, 10014 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10014);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10015, 10001, 10015 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10015);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10016, 10001, 10016 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10016);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10017, 10001, 10017 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10017);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10020, 10001, 10020 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10020);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10021, 10001, 10021 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10021);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10022, 10001, 10022 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10022);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10023, 10001, 10023 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10023);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10024, 10001, 10024 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10024);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10025, 10001, 10025 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10025);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10030, 10001, 10030 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10030);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10031, 10001, 10031 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10031);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10032, 10001, 10032 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10032);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10033, 10001, 10033 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10033);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10034, 10001, 10034 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10034);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10040, 10001, 10040 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10040);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10041, 10001, 10041 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10041);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10042, 10001, 10042 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10042);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10050, 10001, 10050 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10050);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10051, 10001, 10051 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10051);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10060, 10001, 10060 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10060);
INSERT INTO webapi.sec_role_permission (id, role_id, permission_id) SELECT 10070, 10001, 10070 WHERE NOT EXISTS (SELECT 1 FROM webapi.sec_role_permission WHERE id = 10070);

-- CDM data source (broadsea-atlasdb password: mypass)
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
