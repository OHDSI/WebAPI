INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'tool:post', 'Create Tool');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'tool:put', 'Update Tool');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'tool:get', 'List Tools');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'tool:*:get', 'View Tool');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'tool:*:delete', 'View Tool');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
    'tool:post',
    'tool:put',
    'tool:get',
    'tool:*:get',
    'tool:*:delete'
    ) AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
    'tool:get',
    'tool:*:get'
    ) AND sr.name IN ('Atlas users');