INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'cohort-characterization:generation:*:result:export:post', 'Export all cohort characterization generation results' FROM dual;

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'cohort-characterization:generation:*:result:count:get', 'Get total count of results for this generation' FROM dual;

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT ${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
  'cohort-characterization:generation:*:result:export:post',
  'cohort-characterization:generation:*:result:count:get'
)
AND sr.name IN ('Atlas users');

UPDATE ${ohdsiSchema}.sec_permission
SET value = 'cohort-characterization:generation:*:result:post'
WHERE VALUE = 'cohort-characterization:generation:*:result:get'
