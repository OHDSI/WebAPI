-- HONEUR-central role permissions
INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (id, value, description)
  VALUES (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohortdefinition:*:organizations:post', 'Post the list of organizations who can access the definition.');

INSERT INTO ${ohdsiSchema}.sec_role_permission(ID, ROLE_ID, PERMISSION_ID, STATUS)
	VALUES (
	    nextval('${ohdsiSchema}.sec_role_permission_sequence'),
		(SELECT r.id FROM ${ohdsiSchema}.sec_role r WHERE r.name = 'HONEUR-central'),
		(SELECT p.id FROM ${ohdsiSchema}.sec_permission p WHERE p.value = 'cohortdefinition:*:organizations:post'),
		NULL
	);

INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (id, value, description)
  VALUES (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohortdefinition:*:export:get', 'Export the cohort definition to Amazon.');

INSERT INTO ${ohdsiSchema}.sec_role_permission(ID, ROLE_ID, PERMISSION_ID, STATUS)
	VALUES (
	    nextval('${ohdsiSchema}.sec_role_permission_sequence'),
		(SELECT r.id FROM ${ohdsiSchema}.sec_role r WHERE r.name = 'HONEUR-central'),
		(SELECT p.id FROM ${ohdsiSchema}.sec_permission p WHERE p.value = 'cohortdefinition:*:export:get'),
		NULL
	);