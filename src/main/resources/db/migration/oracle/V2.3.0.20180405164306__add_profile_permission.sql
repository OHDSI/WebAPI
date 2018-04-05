INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
  SELECT
    ${ohdsiSchema}.sec_permission_id_seq.nextval,
    'profile:dates:view',
    'View calendar dates on person profiles';

INSERT INTO ${ohdsiSchema}.sec_role_permission (role_id, permission_id)
  SELECT
    sr.id,
    sp.id
  FROM ${ohdsiSchema}.sec_permission sp,
    ${ohdsiSchema}.sec_role sr
  WHERE sp."value" = 'profile:dates:view'
        AND sr.name IN ('admin', 'Atlas users');