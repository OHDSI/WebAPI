-- cc copy permissions

INSERT INTO ${ohdsiSchema}.SEC_PERMISSION(id, "value", "description")
    VALUES (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'cohort-characterization:*:post', '');

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
  SELECT sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'cohort-characterization:*:post'
  ) AND sr.name IN ('Atlas users');

-- pathway copy permissions

INSERT INTO ${ohdsiSchema}.SEC_PERMISSION(id, "value", "description")
VALUES (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'pathway-analysis:*:post', '');

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
  SELECT sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'pathway-analysis:*:post'
  ) AND sr.name IN ('Atlas users');
