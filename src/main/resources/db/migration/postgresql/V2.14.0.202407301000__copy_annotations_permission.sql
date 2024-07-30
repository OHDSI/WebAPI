
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:copy-annotations', 'Copy Concept Set Annotations');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
    'conceptset:copy-annotations'
    ) AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
    'conceptset:copy-annotations'
    ) AND sr.name IN ('Atlas users');

UPDATE ${ohdsiSchema}.sec_permission SET value='conceptset:annotation:*:delete'
WHERE value='conceptset:annotation:%s:delete';