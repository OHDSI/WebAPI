INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'tag:management', 'Permission to manage tags');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
    'tag:management'
    ) AND sr.name IN ('admin');