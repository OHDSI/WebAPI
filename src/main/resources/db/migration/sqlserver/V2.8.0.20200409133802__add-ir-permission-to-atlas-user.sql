INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value = 'ir:*:put' AND sr.name = 'Atlas users';