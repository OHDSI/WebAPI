INSERT INTO  ${ohdsiSchema}.sec_permission (id, value, description)
SELECT nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:check-filter-count:post', 'Access check conceptset filter count for batch Concept Set compare'
WHERE NOT EXISTS (
        SELECT NULL FROM  ${ohdsiSchema}.sec_permission
        WHERE value = 'conceptset:check-filter-count:post'
);

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
    'conceptset:check-filter-count:post'
    ) AND sr.name IN ('admin')
  AND NOT EXISTS (
        SELECT NULL FROM ${ohdsiSchema}.sec_role_permission
        WHERE permission_id = sp.id and role_id = sr.id);


