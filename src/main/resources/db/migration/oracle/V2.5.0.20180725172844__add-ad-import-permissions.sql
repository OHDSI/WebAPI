INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
  SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval,
      'user:providers:get', 'Get list of authentication providers AD/LDAP' FROM dual;

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
  SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
  WHERE sp.value =  'user:providers:get' AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
    SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval,
        'user:import:*:groups:get', 'Search groups in AD/LDAP' FROM dual;

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
    SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, sr.id, sp.id
    FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
    WHERE sp.value = 'user:import:*:groups:get' AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
  SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval,
        'user:import:*:post', 'Search users in AD/LDAP' FROM dual;

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
  SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
  WHERE sp.value = 'user:import:*:post' AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
  SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval,
        'user:import:post', 'Import users from AD/LDAP' FROM dual;

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
  SELECT ${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
  WHERE sp.value = 'user:import:post' AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
  SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval,
        'user:import:*:mapping:post', 'Save Atlas roles mappings to LDAP groups' FROM dual;

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
  SELECT ${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
  WHERE sp.value = 'user:import:*:mapping:post' AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
  SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval,
        'user:import:*:mapping:get', 'Read Atlas roles mappings to LDAP groups' FROM dual;

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
  SELECT ${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
  WHERE sp.value = 'user:import:*:mapping:get' AND sr.name IN ('admin');

CREATE SEQUENCE ${ohdsiSchema}.sec_role_group_seq;

CREATE TABLE ${ohdsiSchema}.sec_role_group(
  id INTEGER PRIMARY KEY NOT NULL,
  provider VARCHAR2(32) NOT NULL,
  group_dn VARCHAR2(2000) NOT NULL,
  group_name VARCHAR2(255),
  role_id INTEGER NOT NULL,
  CONSTRAINT role_group_prov_uniq UNIQUE(provider, group_dn, role_id)
);