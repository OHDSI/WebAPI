INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description) VALUES
(nextval('${ohdsiSchema}.sec_permission_id_seq'), 'review:delegate', 'Delegate approval'),
(nextval('${ohdsiSchema}.sec_permission_id_seq'), 'review:conceptset', 'Approve (conceptset)');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN ('review:delegate', 'review:conceptset') AND sr.name IN ('Moderator');

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description) VALUES
(nextval('${ohdsiSchema}.sec_permission_id_seq'), 'review:conceptset:approved:post', 'View concept sets approval info'),
(nextval('${ohdsiSchema}.sec_permission_id_seq'), 'review:conceptset:*:get', 'Get concept set approval info');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN ('review:conceptset:approved:post', 'review:conceptset:*:get') AND sr.name IN ('Atlas users');
