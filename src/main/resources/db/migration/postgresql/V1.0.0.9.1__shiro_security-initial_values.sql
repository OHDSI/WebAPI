
-- roles
-- 

INSERT INTO SEC_ROLE (ID, NAME) VALUES (1, 'public');
INSERT INTO SEC_ROLE (ID, NAME) VALUES (2, 'admin');
INSERT INTO SEC_ROLE (ID, NAME) VALUES (3, 'concept set editor');
INSERT INTO SEC_ROLE (ID, NAME) VALUES (4, 'concept set reader');
INSERT INTO SEC_ROLE (ID, NAME) VALUES (5, 'cohort editor');
INSERT INTO SEC_ROLE (ID, NAME) VALUES (6, 'cohort reader');

-- permissions 
--

-- public
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (10, 'user:permitted:post', 'Check if user has permission');

INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (1, 10);

-- admin
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (200, 'configuration:read,edit:ui', null);
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (201, 'user:get', 'Get list of users');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (202, 'user:*:permissions:get', 'Get list of user''s permissions');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (203, 'user:*:roles:get', 'Get list of user''s roles');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (204, 'role:put', 'Create role');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (205, 'role:get', 'Get list of roles');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (206, 'role:*:delete', 'Delete role');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (207, 'role:*:permissions:get', 'Get list of role''s permissions');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (208, 'role:*:permissions:*:put', 'Add permission to role');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (209, 'role:*:permissions:*:delete', 'Remove permission from role');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (210, 'role:*:users:get', 'Get list of role''s users');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (211, 'role:*:users:*:put', 'Add user to role');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (212, 'role:*:users:*:delete', 'Remove user from role');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (213, 'permission:put', 'Create permission');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (214, 'permission:get', 'Get list of permissions');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (215, 'permission:*:delete', 'Delete permission');

INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (2, 200);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (2, 201);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (2, 202);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (2, 203);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (2, 204);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (2, 205);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (2, 206);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (2, 207);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (2, 208);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (2, 209);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (2, 210);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (2, 211);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (2, 212);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (2, 213);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (2, 214);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (2, 215);

-- concept set editor
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (30, 'conceptset:edit:ui', null);
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (31, 'conceptset:*:*:exists:get', 'Check if Concept Set exitsts');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (32, 'conceptset:post', 'Create Concept Set');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (33, 'conceptset:*:items:post', 'Save Concept Set items');

INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (3, 30);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (3, 31);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (3, 32);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (3, 33);

-- concept set reader
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (40, 'conceptset:read:ui', null);
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (41, 'conceptset:get', 'Get list of Concept Sets');

INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (4, 40);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (4, 41);

-- cohort editor
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (50, 'cohort:edit:ui', null);
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (51, 'cohortdefinition:put', 'Save new Cohort');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (52, 'job:execution:get', 'Get list of jobs');

INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (5, 50);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (5, 51);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (5, 52);

-- cohort reader
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (60, 'cohort:read:ui', null);
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (61, 'cohortdefinition:get', 'Get list of Cohorts');

INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (6, 60);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (6, 61);
