INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'cohort-characterization:generation:*:result:export:post', 'Export all cohort characterization generation results' FROM dual;

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT ${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
  'cohort-characterization:generation:*:result:export:post'
)
AND sr.name IN ('Atlas users');
