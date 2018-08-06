INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
  SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval,
    'evidence:*:negativecontrols:post',
    'Execute evidence job' from dual;

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
  SELECT ${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
  WHERE sp.value =  'evidence:*:negativecontrols:post' AND sr.name IN ('Atlas users');