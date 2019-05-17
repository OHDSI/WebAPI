INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (${ohdsiSchema}.sec_permission_id_seq.nextval, 'conceptset:*:exists:get', 'Check name uniqueness of concept set');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (${ohdsiSchema}.sec_permission_id_seq.nextval, 'cohortdefinition:*:exists:get', 'Check name uniqueness of cohort definition');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (${ohdsiSchema}.sec_permission_id_seq.nextval, 'pathway-analysis:*:exists:get', 'Check name uniqueness of pathway analysis');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (${ohdsiSchema}.sec_permission_id_seq.nextval, 'cohort-characterization:*:exists:get', 'Check name uniqueness of cohort characterization');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (${ohdsiSchema}.sec_permission_id_seq.nextval, 'feature-analysis:*:exists:get', 'Check name uniqueness of feature analysis');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (${ohdsiSchema}.sec_permission_id_seq.nextval, 'ir:*:exists:get', 'Check name uniqueness of incidence rate');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (${ohdsiSchema}.sec_permission_id_seq.nextval, 'prediction:*:exists:get', 'Check name uniqueness of prediction');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (${ohdsiSchema}.sec_permission_id_seq.nextval, 'estimation:*:exists:get', 'Check name uniqueness of estimation');  

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
WHERE sp.value = 'conceptset:*:exists:get'
AND sr.name IN ('concept set creator', 'Moderator');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
WHERE sp.value = 'cohortdefinition:*:exists:get'
AND sr.name IN ('cohort creator', 'Moderator');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
  'pathway-analysis:*:exists:get',
  'cohort-characterization:*:exists:get',
  'feature-analysis:*:exists:get',
  'ir:*:exists:get',
  'prediction:*:exists:get',
  'estimation:*:exists:get'
)
AND sr.name IN ('Atlas users', 'Moderator');