-- Estimation

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'estimation:post', 'Create Estimation' FROM dual;
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'estimation:get', 'Get Estimation list' FROM dual;
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'estimation:*:get', 'Get Estimation instance' FROM dual;
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'estimation:*:copy:get', 'Copy Estimation instance' FROM dual;
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'estimation:*:download:get', 'Download Estimation package' FROM dual;
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'estimation:*:export:get', 'Export Estimation' FROM dual;

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT ${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
  'estimation:post',
  'estimation:get',
  'estimation:*:get',
  'estimation:*:copy:get',
  'estimation:*:download:get',
  'estimation:*:export:get'
)
AND sr.name IN ('Atlas users');

-- Prediction

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'prediction:post', 'Create Prediction' FROM dual;
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'prediction:get', 'Get Prediction list' FROM dual;
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'prediction:*:get', 'Get Prediction instance' FROM dual;
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'prediction:*:copy:get', 'Copy Prediction instance' FROM dual;
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'prediction:*:download:get', 'Download Prediction package' FROM dual;
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'prediction:*:export:get', 'Export Prediction' FROM dual;

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT ${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
  'prediction:post',
  'prediction:get',
  'prediction:*:get',
  'prediction:*:copy:get',
  'prediction:*:download:get',
  'prediction:*:export:get'
)
AND sr.name IN ('Atlas users');