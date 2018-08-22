INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
  SELECT
    ${ohdsiSchema}.sec_permission_id_seq.nextval,
    '*:person:*:get:dates',
    'View calendar dates on person profiles' FROM dual;

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
  SELECT
    ${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval,
    sr.id,
    sp.id
  FROM ${ohdsiSchema}.sec_permission sp,
    ${ohdsiSchema}.sec_role sr
  WHERE sp.value = '*:person:*:get:dates'
        AND sr.name IN ('admin', 'Atlas users');