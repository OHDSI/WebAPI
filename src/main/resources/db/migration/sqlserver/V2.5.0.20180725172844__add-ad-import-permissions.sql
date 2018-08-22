INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
  SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq,
      'user:providers:get', 'Get list of authentication providers AD/LDAP';

INSERT INTO ${ohdsiSchema}.sec_role_permission (role_id, permission_id)
  SELECT sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
  WHERE sp."value" =  'user:providers:get' AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
    SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq,
        'user:import:*:groups:get', 'Search groups in AD/LDAP';

INSERT INTO ${ohdsiSchema}.sec_role_permission (role_id, permission_id)
    SELECT sr.id, sp.id
    FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
    WHERE sp."value" = 'user:import:*:groups:get' AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
  SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq,
        'user:import:*:post', 'Search users in AD/LDAP';

INSERT INTO ${ohdsiSchema}.sec_role_permission (role_id, permission_id)
  SELECT sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
  WHERE sp."value" = 'user:import:*:post' AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
  SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq,
        'user:import:post', 'Import users from AD/LDAP';

INSERT INTO ${ohdsiSchema}.sec_role_permission (role_id, permission_id)
  SELECT sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
  WHERE sp."value" = 'user:import:post' AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
  SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq,
        'user:import:*:mapping:post', 'Save Atlas roles mappings to LDAP groups';

INSERT INTO ${ohdsiSchema}.sec_role_permission (role_id, permission_id)
  SELECT sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
  WHERE sp."value" = 'user:import:*:mapping:post' AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
  SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq,
        'user:import:*:mapping:get', 'Read Atlas roles mappings to LDAP groups';

INSERT INTO ${ohdsiSchema}.sec_role_permission (role_id, permission_id)
  SELECT sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
  WHERE sp."value" = 'user:import:*:mapping:get' AND sr.name IN ('admin');

CREATE SEQUENCE ${ohdsiSchema}.sec_role_group_seq;

CREATE TABLE ${ohdsiSchema}.sec_role_group(
  id INTEGER PRIMARY KEY DEFAULT NEXT VALUE FOR ${ohdsiSchema}.sec_role_group_seq,
  provider VARCHAR NOT NULL,
  group_dn VARCHAR NOT NULL,
  group_name VARCHAR,
  role_id INTEGER NOT NULL,
  CONSTRAINT UC_PROVIDER_GROUP_ROLE UNIQUE(provider, group_dn, role_id)
);