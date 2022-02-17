INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohortdefinition:checkv2:post', 'Run diagnostics for cohort definition with tags');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
    'cohortdefinition:checkv2:post'
    ) AND sr.name IN ('Atlas users');