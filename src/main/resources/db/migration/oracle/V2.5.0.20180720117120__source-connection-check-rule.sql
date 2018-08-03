INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES(${ohdsiSchema}.sec_permission_id_seq.nextval, 'source:connection:*:get', 'Check source connection');
INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
  SELECT sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
  WHERE sp."value" = 'source:connection:*:get' AND sr.name IN ('admin');