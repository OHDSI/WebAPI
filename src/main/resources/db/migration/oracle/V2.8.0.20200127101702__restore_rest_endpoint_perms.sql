INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'source:priorityVocabulary:get', 'Get source with highest priority vocabulary daimon' FROM DUAL
;

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'cohort-characterization:generation:*:result:get', 'Get cohort characterization generation results - 2.7.x compatible' FROM DUAL
;

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
  'source:priorityVocabulary:get',
  'cohort-characterization:generation:*:result:get'
)
AND sr.name IN ('Atlas users', 'Moderator')
;