INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohort-characterization:*:check:get', 'Check cohort characterization params');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohort-characterization:*:check:post', 'Run diagnostics for cohort characterization params');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'pathway-analysis:*:check:get', 'Check pathway params');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'pathway-analysis:*:check:post', 'Run diagnostics for pathway params');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'ir:*:check:get', 'Check incident rates params');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'ir:*:check:post', 'Run diagnostics for incident rates');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'prediction:*:check:get', 'Check prediction params');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'prediction:*:check:post', 'Run diagnostics for prediction');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'estimation:*:check:get', 'Check estimation params');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'estimation:*:check:post', 'Run diagnostics for estimation');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'cohort-characterization:*:check:get',
    'cohort-characterization:*:check:post',
    'pathway-analysis:*:check:get',
    'pathway-analysis:*:check:post',
    'ir:*:check:get',
    'ir:*:check:post',
    'prediction:*:check:get',
    'prediction:*:check:post',
    'estimation:*:check:get',
    'estimation:*:check:post'
  ) AND sr.name IN ('Atlas users');