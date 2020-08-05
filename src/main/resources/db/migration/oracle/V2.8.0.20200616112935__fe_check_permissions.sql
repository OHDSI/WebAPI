INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (ID, VALUE, DESCRIPTION)
  VALUES (${ohdsiSchema}.sec_permission_id_seq.nextval, 'feature-analysis:*:copy:get', 'Copy the specified feature analysis');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'feature-analysis:*:copy:get'
  ) AND sr.name IN ('Atlas users');