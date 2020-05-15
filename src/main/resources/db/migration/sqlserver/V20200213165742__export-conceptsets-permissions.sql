INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
  SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'feature-analysis:*:export:conceptset:get', 'Get archive with Feature Analysis Concept Sets'
;

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
  SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'cohort-characterization:*:export:conceptset:get', 'Get archive with Cohort Characterization Concept Sets'
;

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE
    sp.value IN (
    'feature-analysis:*:export:conceptset:get',
    'cohort-characterization:*:export:conceptset:get'
    )
    AND sr.name IN ('Atlas users')
;
