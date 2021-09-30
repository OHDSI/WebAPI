INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
VALUES
  (${ohdsiSchema}.sec_permission_id_seq.nextval, 'conceptset:*:expression:*:get', 'Resolve concept set expression with data source');
  
INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
  'conceptset:*:expression:*:get'
)
AND sr.name IN ('Atlas users'); 