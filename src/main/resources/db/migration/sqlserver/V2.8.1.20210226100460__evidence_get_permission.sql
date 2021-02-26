INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
  SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq,'evidence:*:drugconditionpairs:post','Get drug condition pairs information';

INSERT INTO ${ohdsiSchema}.sec_role_permission (role_id, permission_id)
  SELECT sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
  WHERE sp."value" in
  (
    'evidence:*:drugconditionpairs:post'
  )
  AND sr.name IN ('Atlas users');