INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES(58, 'cohortdefinition:*:put', 'Update cohort definition');
UPDATE ${ohdsiSchema}.sec_role_permission
    SET permission_id = 58 WHERE permission_id = 1004;
DELETE FROM ${ohdsiSchema}.sec_permission WHERE id = 1004;

INSERT INTO ${ohdsiSchema}.sec_role(id, name) VALUES (10, 'Atlas users') ON CONFLICT DO NOTHING;
INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
    SELECT 10, id
    FROM ${ohdsiSchema}.sec_permission
    WHERE id < 200 ON CONFLICT DO NOTHING;