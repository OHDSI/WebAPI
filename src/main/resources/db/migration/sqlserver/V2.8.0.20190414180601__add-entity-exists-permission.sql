INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
VALUES
  (NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, 'conceptset:*:exists:get', 'Check name uniqueness of concept set'), 
  (NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, 'cohortdefinition:*:exists:get', 'Check name uniqueness of cohort definition'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, 'pathway-analysis:*:exists:get', 'Check name uniqueness of pathway analysis'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, 'cohort-characterization:*:exists:get', 'Check name uniqueness of cohort characterization'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, 'feature-analysis:*:exists:get', 'Check name uniqueness of feature analysis'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, 'ir:*:exists:get', 'Check name uniqueness of incidence rate'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, 'prediction:*:exists:get', 'Check name uniqueness of prediction'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, 'estimation:*:exists:get', 'Check name uniqueness of estimation');


INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value = 'conceptset:*:exists:get'
AND sr.name IN ('concept set creator', 'Moderator');


INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value = 'cohortdefinition:*:exists:get'
AND sr.name IN ('cohort creator', 'Moderator');



INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
  'pathway-analysis:*:exists:get',
  'cohort-characterization:*:exists:get',
  'feature-analysis:*:exists:get',
  'ir:*:exists:get',
  'prediction:*:exists:get',
  'estimation:*:exists:get'
)
AND sr.name IN ('Atlas users', 'Moderator');