INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (${ohdsiSchema}.sec_permission_id_seq.nextval, 'cohort-characterization:check:post', 'Run diagnostics for cohort characterization params');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (${ohdsiSchema}.sec_permission_id_seq.nextval, 'pathway-analysis:check:post', 'Run diagnostics for pathway params');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (${ohdsiSchema}.sec_permission_id_seq.nextval, 'ir:check:post', 'Run diagnostics for incident rates');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (${ohdsiSchema}.sec_permission_id_seq.nextval, 'prediction:check:post', 'Run diagnostics for prediction');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (${ohdsiSchema}.sec_permission_id_seq.nextval, 'estimation:check:post', 'Run diagnostics for estimation');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (${ohdsiSchema}.sec_permission_id_seq.nextval, 'cohortdefinition:check:post', 'Run diagnostics for cohort definition');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'cohort-characterization:check:post',
    'pathway-analysis:check:post',
    'ir:check:post',
    'prediction:check:post',
    'estimation:check:post',
    'cohortdefinition:check:post'
  ) AND sr.name IN ('Atlas users');