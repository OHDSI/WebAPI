INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
  SELECT nextval('${ohdsiSchema}.sec_permission_id_seq'),'evidence:*:negativecontrols:*:get','Get evidence information';
  INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
  SELECT nextval('${ohdsiSchema}.sec_permission_id_seq'),'evidence:*:druglabel:post','Get drug label information';

INSERT INTO ${ohdsiSchema}.sec_role_permission (role_id, permission_id)
  SELECT sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
  WHERE sp."value" in
  (
    'evidence:*:negativecontrols:*:get',
    'evidence:*:druglabel:post'
  )
  AND sr.name IN ('Atlas users');