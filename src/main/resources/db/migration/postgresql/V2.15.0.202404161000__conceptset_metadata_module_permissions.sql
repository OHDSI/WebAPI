INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:update:*:metadata:put', 'Update Concept Set Metadata');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:*:metadata:put', 'Create Concept Set Metadata');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:%s:delete', 'Delete Concept Set Metadata');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:%s:metadata:get', 'List Concept Set Metadata');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:*:metadata:get', 'View Concept Set Metadata');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
    'conceptset:update:*:metadata:put',
    'conceptset:*:metadata:put',
    'conceptset:%s:delete',
    'conceptset:%s:metadata:get',
    'conceptset:*:metadata:get'
    ) AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
    'conceptset:%s:metadata:get',
    'conceptset:*:metadata:get'
    ) AND sr.name IN ('Atlas users');