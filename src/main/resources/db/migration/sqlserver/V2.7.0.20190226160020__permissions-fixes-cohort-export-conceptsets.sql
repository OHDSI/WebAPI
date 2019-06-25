INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'cohortdefinition:*:export:conceptset:get', 'Export ConceptSet');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
  'cohortdefinition:*:export:conceptset:get'
) AND sr.name IN ('Atlas users');