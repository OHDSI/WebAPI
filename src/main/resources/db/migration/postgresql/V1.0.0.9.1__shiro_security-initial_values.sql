
-- roles
-- 

INSERT INTO SEC_ROLE (ID, NAME) VALUES (1, 'public');
INSERT INTO SEC_ROLE (ID, NAME) VALUES (2, 'admin');
INSERT INTO SEC_ROLE (ID, NAME) VALUES (3, 'concept set editor');
--INSERT INTO SEC_ROLE (ID, NAME) VALUES (4, 'concept set reader');
INSERT INTO SEC_ROLE (ID, NAME) VALUES (5, 'cohort editor');
INSERT INTO SEC_ROLE (ID, NAME) VALUES (6, 'cohort reader');

-- permissions 
--

-- public
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (10, 'user:permitted:post', 'Check if user has permission');

INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (1, 10);

-- admin
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (200, 'configuration:edit:ui', 'Access to ''Configuration'' page');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (201, 'user:get', 'Get list of users');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (202, 'permission:get', 'Get list of permissions');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (203, 'role:put', 'Create role');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (204, 'role:get', 'Get list of roles');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (205, 'role:*:get', 'Get role by ID');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (206, 'role:*:permissions:get', 'Get list of role''s permissions');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (207, 'role:*:users:get', 'Get list of role''s users');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (208, 'role:*:users:*:put', 'Add users to role');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (209, 'role:*:users:*:delete', 'Remove users from role');

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

-- concept set editor
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (30, 'conceptset:edit:ui', null);
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (31, 'conceptset:*:*:exists:get', 'Check if Concept Set exists');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (32, 'conceptset:put', 'Create Concept Set');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (33, 'conceptset:*:items:post', 'Save Concept Set items');

INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (3, 30);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (3, 31);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (3, 32);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (3, 33);

-- cohort editor
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (50, 'cohort:edit:ui', null);
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (51, 'cohortdefinition:put', 'Save new Cohort');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (52, 'job:execution:get', 'Get list of jobs');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (53, 'cohortdefinition:*:copy:get', 'Copy the specified cohort definition');

INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (5, 50);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (5, 51);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (5, 52);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (5, 53);

-- cohort reader
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (60, 'cohortdefinition:get', 'Get list of Cohorts');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (61, 'cohortdefinition:*:get', 'Get Cohort Definition by ID');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (62, 'cohortdefinition:*:info:get', '');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (63, '*:vocabulary:lookup:identifiers:post', 'Perform a lookup of an array of concept identifiers returning the matching concepts with their detailed properties.');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (64, 'cohortdefinition:sql:post', 'Generate SQL from Cohort expression');
INSERT INTO SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (65, '*:cohortresults:*:breakdown:get', 'Get breakdown with counts about people in cohort');

INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (6, 60);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (6, 61);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (6, 62);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (6, 63);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (6, 64);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (6, 65);
