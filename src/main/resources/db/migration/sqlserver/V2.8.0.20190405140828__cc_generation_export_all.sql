INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
VALUES
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'cohort-characterization:generation:*:result:export:post', 'Export all cohort characterization generation results'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'cohort-characterization:generation:*:result:count:get', 'Get total count of results for this generation');

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
  'cohort-characterization:generation:*:result:export:post',
  'cohort-characterization:generation:*:result:count:get'
)
AND sr.name IN ('Atlas users');

UPDATE ${ohdsiSchema}.sec_permission
SET value = 'cohort-characterization:generation:*:result:post'
WHERE VALUE = 'cohort-characterization:generation:*:result:get'