-- HONEUR-central role
INSERT INTO ${ohdsiSchema}.SEC_ROLE (ID, NAME) VALUES (nextval('sec_role_sequence'), 'HONEUR-central');

-- HONEUR-central role permissions
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID, STATUS)
  VALUES (nextval('${ohdsiSchema}.sec_role_permission_sequence'),currval('${ohdsiSchema}.sec_role_sequence'),(
      SELECT p.id FROM ${ohdsiSchema}.SEC_PERMISSION p WHERE p.value = 'cohortdefinition:post'
    ),NULL );
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID, STATUS)
  VALUES (nextval('${ohdsiSchema}.sec_role_permission_sequence'),currval('${ohdsiSchema}.sec_role_sequence'),(
      SELECT p.id FROM ${ohdsiSchema}.SEC_PERMISSION p WHERE p.value = 'job:execution:get'
    ),NULL );
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID, STATUS)
  VALUES (nextval('${ohdsiSchema}.sec_role_permission_sequence'),currval('${ohdsiSchema}.sec_role_sequence'),(
      SELECT p.id FROM ${ohdsiSchema}.SEC_PERMISSION p WHERE p.value = 'cohortdefinition:get'
    ),NULL );
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID, STATUS)
  VALUES (nextval('${ohdsiSchema}.sec_role_permission_sequence'),currval('${ohdsiSchema}.sec_role_sequence'),(
      SELECT p.id FROM ${ohdsiSchema}.SEC_PERMISSION p WHERE p.value = 'cohortdefinition:*:info:get'
    ),NULL );
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID, STATUS)
  VALUES (nextval('${ohdsiSchema}.sec_role_permission_sequence'),currval('${ohdsiSchema}.sec_role_sequence'),(
      SELECT p.id FROM ${ohdsiSchema}.SEC_PERMISSION p WHERE p.value = 'cohortdefinition:sql:post'
    ),NULL );
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID, STATUS)
  VALUES (nextval('${ohdsiSchema}.sec_role_permission_sequence'),currval('${ohdsiSchema}.sec_role_sequence'),(
      SELECT p.id FROM ${ohdsiSchema}.SEC_PERMISSION p WHERE p.value = '*:vocabulary:lookup:identifiers:post'
    ),NULL );
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID, STATUS)
  VALUES (nextval('${ohdsiSchema}.sec_role_permission_sequence'),currval('${ohdsiSchema}.sec_role_sequence'),(
      SELECT p.id FROM ${ohdsiSchema}.SEC_PERMISSION p WHERE p.value = '*:cohortresults:*:breakdown:get'
    ),NULL );

-- HONEUR-central role - Honeur specific permissions
INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (id, value, description)
  VALUES (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohortdefinition:*:organizations:get', 'Get the list of organizations who can access the definition.');
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID, STATUS)
  VALUES (nextval('${ohdsiSchema}.sec_role_permission_sequence'),currval('${ohdsiSchema}.sec_role_sequence'),currval('${ohdsiSchema}.sec_permission_id_seq'),NULL );

INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (id, value, description)
  VALUES (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'user:permission:get', 'Get the list of permissions for the logged in user.');
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID, STATUS)
  VALUES (nextval('${ohdsiSchema}.sec_role_permission_sequence'),currval('${ohdsiSchema}.sec_role_sequence'),currval('${ohdsiSchema}.sec_permission_id_seq'),NULL );

-- HONEUR-local role
INSERT INTO ${ohdsiSchema}.SEC_ROLE (ID, NAME) VALUES (nextval('sec_role_sequence'), 'HONEUR-local');

-- HONEUR-local role permissions
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID, STATUS)
  VALUES (nextval('${ohdsiSchema}.sec_role_permission_sequence'),currval('${ohdsiSchema}.sec_role_sequence'),(
      SELECT p.id FROM ${ohdsiSchema}.SEC_PERMISSION p WHERE p.value = 'cohortdefinition:get'
    ),NULL );

-- 'user:permission:get' permission
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID, STATUS)
  VALUES (nextval('${ohdsiSchema}.sec_role_permission_sequence'),currval('${ohdsiSchema}.sec_role_sequence'),currval('${ohdsiSchema}.sec_permission_id_seq'),NULL );

-- HONEUR-local role - Honeur specific permissions
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID, STATUS)
  VALUES (
    nextval('${ohdsiSchema}.sec_role_permission_sequence'),
    currval('${ohdsiSchema}.sec_role_sequence'),
    (
      SELECT p.id FROM ${ohdsiSchema}.SEC_PERMISSION p WHERE p.value = 'cohortdefinition:*:organizations:get'
    ),
    NULL
  );
INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (id, value, description)
  VALUES (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohortdefinition:*:export:*:get', 'Export the cohort definition generation results to Amazon.');
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID, STATUS)
  VALUES (nextval('${ohdsiSchema}.sec_role_permission_sequence'),currval('${ohdsiSchema}.sec_role_sequence'),currval('${ohdsiSchema}.sec_permission_id_seq'),NULL );

INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (id, value, description)
  VALUES (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohortdefinition:hss:list:all:get', 'Get the cohortdefinitions in the HSS for the logged in user.');
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID, STATUS)
  VALUES (nextval('${ohdsiSchema}.sec_role_permission_sequence'),currval('${ohdsiSchema}.sec_role_sequence'),currval('${ohdsiSchema}.sec_permission_id_seq'),NULL );

INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (id, value, description)
  VALUES (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohortdefinition:hss:select:post', 'Create the selected cohortdefinition from the HSS in Atlas.');
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID, STATUS)
  VALUES (nextval('${ohdsiSchema}.sec_role_permission_sequence'),currval('${ohdsiSchema}.sec_role_sequence'),currval('${ohdsiSchema}.sec_permission_id_seq'),NULL );
