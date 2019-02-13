-- cc copy permissions

INSERT INTO ${ohdsiSchema}.SEC_PERMISSION(id, value, description)
    VALUES (${ohdsiSchema}.sec_permission_id_seq.nextval, 'cohort-characterization:*:post', '');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'cohort-characterization:*:post'
  ) AND sr.name IN ('Atlas users');

-- pathway copy permissions

INSERT INTO ${ohdsiSchema}.SEC_PERMISSION(id, value, description)
VALUES (${ohdsiSchema}.sec_permission_id_seq.nextval, 'pathway-analysis:*:post', '');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'pathway-analysis:*:post'
  ) AND sr.name IN ('Atlas users');