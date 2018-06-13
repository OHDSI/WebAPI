INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (id, value, description)
  VALUES (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohortdefinition:uuids:get', 'Get the list of uuids of cohort definitions for the logged in user.');

INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID, STATUS)
  VALUES (
    nextval('${ohdsiSchema}.sec_role_permission_sequence'),
    (
      SELECT r.id FROM ${ohdsiSchema}.SEC_ROLE r WHERE r.name = 'HONEUR-local'
    ),
    (
      SELECT p.id FROM ${ohdsiSchema}.SEC_PERMISSION p WHERE p.value = 'cohortdefinition:uuids:get'
    ),
    NULL
  );