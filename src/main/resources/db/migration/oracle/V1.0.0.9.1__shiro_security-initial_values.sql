
-- roles
-- 

INSERT INTO ${ohdsiSchema}.SEC_ROLE (ID, NAME) VALUES (1, 'public');
INSERT INTO ${ohdsiSchema}.SEC_ROLE (ID, NAME) VALUES (2, 'admin');
INSERT INTO ${ohdsiSchema}.SEC_ROLE (ID, NAME) VALUES (3, 'concept set creator');
--INSERT INTO ${ohdsiSchema}.SEC_ROLE (ID, NAME) VALUES (4, 'concept set reader');
INSERT INTO ${ohdsiSchema}.SEC_ROLE (ID, NAME) VALUES (5, 'cohort creator');
INSERT INTO ${ohdsiSchema}.SEC_ROLE (ID, NAME) VALUES (6, 'cohort reader');

-- permissions 
--

-- admin
INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (200, 'configuration:edit:ui', 'Access to ''Configuration'' page');
INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (201, 'user:get', 'Get list of users');
INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (202, 'permission:get', 'Get list of permissions');
INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (203, 'role:post', 'Create role');
INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (204, 'role:get', 'Get list of roles');
INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (205, 'role:*:get', 'Get role by ID');
INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (206, 'role:*:permissions:get', 'Get list of role''s permissions');
INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (207, 'role:*:users:get', 'Get list of role''s users');
INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (208, 'role:*:users:*:put', 'Add users to role');
INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (209, 'role:*:users:*:delete', 'Remove users from role');
INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (210, 'role:1:permissions:*:put', 'Add permissions to public role');
INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (211, 'role:1:permissions:*:delete', 'Remove permissions from public role');

INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID) VALUES (${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, 2, 200);
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID) VALUES (${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, 2, 201);
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID) VALUES (${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, 2, 202);
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID) VALUES (${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, 2, 203);
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID) VALUES (${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, 2, 204);
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID) VALUES (${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, 2, 205);
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID) VALUES (${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, 2, 206);
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID) VALUES (${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, 2, 207);
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID) VALUES (${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, 2, 208);
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID) VALUES (${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, 2, 209);
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID) VALUES (${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, 2, 210);
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID) VALUES (${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, 2, 211);

-- concept set creator
INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (30, 'conceptset:post', 'Create Concept Set');

INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID) VALUES (${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, 3, 30);

-- cohort creator
INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (50, 'cohortdefinition:post', 'Save new Cohort Definition');
INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (51, 'job:execution:get', 'Get list of jobs');
INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (52, 'cohortdefinition:*:copy:get', 'Copy the specified cohort definition');

INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID) VALUES (${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, 5, 50);
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID) VALUES (${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, 5, 51);
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID) VALUES (${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, 5, 52);

-- cohort reader
INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (60, 'cohortdefinition:get', 'Get list of Cohort Definitions');
INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (61, 'cohortdefinition:*:get', 'Get Cohort Definition by ID');
INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (62, 'cohortdefinition:*:info:get', '');
INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (63, '*:vocabulary:lookup:identifiers:post', 'Perform a lookup of an array of concept identifiers returning the matching concepts with their detailed properties.');
INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (64, 'cohortdefinition:sql:post', 'Generate SQL from Cohort expression');
INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (65, '*:cohortresults:*:breakdown:get', 'Get breakdown with counts about people in cohort');

INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID) VALUES (${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, 6, 60);
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID) VALUES (${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, 6, 61);
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID) VALUES (${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, 6, 62);
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID) VALUES (${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, 6, 63);
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID) VALUES (${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, 6, 64);
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID) VALUES (${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, 6, 65);
