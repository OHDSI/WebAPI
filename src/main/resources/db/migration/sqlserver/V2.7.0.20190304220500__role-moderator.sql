INSERT INTO ${ohdsiSchema}.sec_role(id, name, system_role) VALUES
  (NEXT VALUE FOR ${ohdsiSchema}.sec_role_sequence, 'Moderator', 1);

insert into ${ohdsiSchema}.sec_permission(id, value, description)
values
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'cohort-characterization:*:put', 'Edit any Cohort Characterization'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'cohort-characterization:*:delete', 'Delete any Cohort Characterization'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'pathway-analysis:*:put', 'Edit any Pathways analysis'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'pathway-analysis:*:delete', 'Delete any Pathways analysis'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'ir:*:put', 'Edit any IR analysis'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'ir:*:info:*:delete', 'Delete any IR analysis'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'estimation:*:put', 'Edit any Estimation analysis'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'estimation:*:delete', 'Delete any Estimation analysis'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'prediction:*:put', 'Edit any Prediction analysis'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'prediction:*:delete', 'Delete any Prediction analysis');

-- 16 perms
INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
  -- All Concept Sets edit and delete
  'conceptset:*:delete', 'conceptset:*:put', 'conceptset:*:items:put',
  -- All Cohort Definitions edit and delete
  'cohortdefinition:*:put', 'cohortdefinition:*:delete',
  -- All CC edit and delete
  'cohort-characterization:*:put', 'cohort-characterization:*:delete',
  -- All Pathways edit and delete
  'pathway-analysis:*:put', 'pathway-analysis:*:delete',
  -- All IRs edit and delete
  'ir:*:put', 'ir:*:delete', 'ir:*:info:*:delete',
  -- All Estimation analyses edit and delete
  'estimation:*:put', 'estimation:*:delete',
  -- All Prediction analyses edit and delete
  'prediction:*:put', 'prediction:*:delete'
) AND sr.name IN ('Moderator');