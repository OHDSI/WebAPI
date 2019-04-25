INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
VALUES
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:*:exists:get', 'Check name uniqueness of concept set'), 
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohortdefinition:*:exists:get', 'Check name uniqueness of cohort definition'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'pathway-analysis:*:exists:get', 'Check name uniqueness of pathway analysis'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohort-characterization:*:exists:get', 'Check name uniqueness of cohort characterization'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'feature-analysis:*:exists:get', 'Check name uniqueness of feature analysis'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'ir:*:exists:get', 'Check name uniqueness of incidence rate'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'prediction:*:exists:get', 'Check name uniqueness of prediction'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'estimation:*:exists:get', 'Check name uniqueness of estimation');


INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value = 'conceptset:*:exists:get'
AND sr.name IN ('concept set creator', 'Moderator');


INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value = 'cohortdefinition:*:exists:get'
AND sr.name IN ('cohort creator', 'Moderator');



INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
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
