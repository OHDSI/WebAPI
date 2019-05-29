INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
VALUES
  (NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, 'conceptset:*:copy-name:get', 'Get name for copying concept set');


INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value = 'conceptset:*:copy-name:get'
AND sr.name IN ('concept set creator', 'Moderator', 'Atlas users');