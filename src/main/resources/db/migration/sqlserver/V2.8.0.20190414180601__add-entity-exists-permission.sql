INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
VALUES
  (NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, 'conceptset:*:exists', 'Check name uniqueness of concept set'), 
  (NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, 'cohortdefinition:*:exists', 'Check name uniqueness of cohort definition'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, 'pathway-analysis:*:exists', 'Check name uniqueness of pathway analysis'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, 'cohort-characterization:*:exists', 'Check name uniqueness of cohort characterization'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, 'feature-analysis:*:exists', 'Check name uniqueness of feature analysis'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, 'ir:*:exists', 'Check name uniqueness of incidence rate'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, 'prediction:*:exists', 'Check name uniqueness of prediction'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, 'estimation:*:exists', 'Check name uniqueness of estimation');


INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value = 'conceptset:*:exists'
AND sr.name IN ('concept set creator', 'Moderator');


INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value = 'cohortdefinition:*:exists'
AND sr.name IN ('cohort creator', 'Moderator');



INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
  'pathway-analysis:*:exists',
  'cohort-characterization:*:exists',
  'feature-analysis:*:exists',
  'ir:*:exists',
  'prediction:*:exists',
  'estimation:*:exists'
)
AND sr.name IN ('Atlas users', 'Moderator');