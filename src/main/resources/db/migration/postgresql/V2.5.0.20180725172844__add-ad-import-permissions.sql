INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
  VALUES (nextval('${ohdsiSchema}.sec_permission_id_seq'),
      'user:providers:get', 'Get list of authentication providers AD/LDAP');

INSERT INTO ${ohdsiSchema}.sec_role_permission (role_id, permission_id)
  SELECT sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
  WHERE sp."value" =  'user:providers:get' AND sr.name IN ('admin');

