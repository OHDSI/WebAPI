INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
  SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq,
        'user:import:*:test:get', 'Check LDAP/AD connection';

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
  SELECT NEXT VALUE FOR ${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE, sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
  WHERE sp."value" = 'user:import:*:test:get' AND sr.name IN ('admin');
