INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (ID, VALUE, DESCRIPTION) VALUES (1004, 'cohortdefinition:*:put', 'Update cohort definition');

INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ID, ROLE_ID, PERMISSION_ID) VALUES (1026, 5, 1004);