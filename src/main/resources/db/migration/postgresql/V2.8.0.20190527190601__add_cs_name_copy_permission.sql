INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
VALUES
  (nextval('sec_permission_id_seq'), 'conceptset:*:copy-name:get', 'Get name for copying concept set');


INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value = 'conceptset:*:copy-name:get'
AND sr.name IN ('concept set creator', 'Moderator', 'Atlas users');