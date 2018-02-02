INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES(58, 'cohortdefinition:*:put', 'Update cohort definition');
INSERT INTO ${ohdsiSchema}.SEC_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES (5, 58);

INSERT INTO ${ohdsiSchema}.sec_role(id, name) VALUES (10, 'Atlas users');
INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
    SELECT 10, id
    FROM ${ohdsiSchema}.sec_permission
    WHERE id < 200;