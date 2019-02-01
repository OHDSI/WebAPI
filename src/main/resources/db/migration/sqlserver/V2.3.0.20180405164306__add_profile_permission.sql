INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
  SELECT
    NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq,
    '*:person:*:get:dates',
    'View calendar dates on person profiles';

INSERT INTO ${ohdsiSchema}.sec_role_permission (role_id, permission_id)
  SELECT
    sr.id,
    sp.id
  FROM ${ohdsiSchema}.sec_permission sp,
    ${ohdsiSchema}.sec_role sr
  WHERE sp."value" = '*:person:*:get:dates'
        AND sr.name IN ('admin', 'Atlas users');