INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohort-characterization:*:design:get', 'Get cohort characterization design');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohort-characterization:design:get', 'Get cohort characterization design list');


INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'cohort-characterization:*:design:get',
    'cohort-characterization:design:get'
  )
  AND sr.name IN ('Atlas users');