INSERT INTO ${ohdsiSchema}.sec_role(id, name, system_role) VALUES
  (${ohdsiSchema}.sec_role_sequence.nextval, 'Moderator', 1);

insert into ${ohdsiSchema}.sec_permission(id, value, description)
values (${ohdsiSchema}.sec_permission_id_seq.nextval, 'cohort-characterization:*:put', 'Edit any Cohort Characterization');

insert into ${ohdsiSchema}.sec_permission(id, value, description)
values (${ohdsiSchema}.sec_permission_id_seq.nextval, 'cohort-characterization:*:delete', 'Delete any Cohort Characterization');

insert into ${ohdsiSchema}.sec_permission(id, value, description)
values (${ohdsiSchema}.sec_permission_id_seq.nextval, 'pathway-analysis:*:put', 'Edit any Pathways analysis');

insert into ${ohdsiSchema}.sec_permission(id, value, description)
values (${ohdsiSchema}.sec_permission_id_seq.nextval, 'pathway-analysis:*:delete', 'Delete any Pathways analysis');

insert into ${ohdsiSchema}.sec_permission(id, value, description)
values (${ohdsiSchema}.sec_permission_id_seq.nextval, 'ir:*:put', 'Edit any IR analysis');

insert into ${ohdsiSchema}.sec_permission(id, value, description)
values (${ohdsiSchema}.sec_permission_id_seq.nextval, 'ir:*:info:*:delete', 'Delete any IR analysis');

insert into ${ohdsiSchema}.sec_permission(id, value, description)
values (${ohdsiSchema}.sec_permission_id_seq.nextval, 'estimation:*:put', 'Edit any Estimation analysis');

insert into ${ohdsiSchema}.sec_permission(id, value, description)
values (${ohdsiSchema}.sec_permission_id_seq.nextval, 'estimation:*:delete', 'Delete any Estimation analysis');

insert into ${ohdsiSchema}.sec_permission(id, value, description)
values (${ohdsiSchema}.sec_permission_id_seq.nextval, 'prediction:*:put', 'Edit any Prediction analysis');

insert into ${ohdsiSchema}.sec_permission(id, value, description)
values (${ohdsiSchema}.sec_permission_id_seq.nextval, 'prediction:*:delete', 'Delete any Prediction analysis');


INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sr.id, sp.id
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