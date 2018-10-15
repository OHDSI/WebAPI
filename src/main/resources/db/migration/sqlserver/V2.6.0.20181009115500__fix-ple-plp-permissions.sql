-- Estimation

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
VALUES
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'estimation:post', 'Create Estimation'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'estimation:get', 'Get Estimation list'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'estimation:*:get', 'Get Estimation instance'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'estimation:*:copy:get', 'Copy Estimation instance'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'estimation:*:download:get', 'Download Estimation package'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'estimation:*:export:get', 'Export Estimation');

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
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
VALUES
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'prediction:post', 'Create Prediction'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'prediction:get', 'Get Prediction list'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'prediction:*:get', 'Get Prediction instance'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'prediction:*:copy:get', 'Copy Prediction instance'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'prediction:*:download:get', 'Download Prediction package'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'prediction:*:export:get', 'Export Prediction');

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
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