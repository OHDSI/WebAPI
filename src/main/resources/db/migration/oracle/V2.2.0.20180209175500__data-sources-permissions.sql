INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
VALUES (${ohdsiSchema}.sec_permission_id_seq.nextval, 'cdmresults:*:*:get', 'View CDM results');

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
  SELECT
    ${ohdsiSchema}.sec_role_permission_sequence.NEXTVAL,
    sr.id,
    sp.id
  FROM ${ohdsiSchema}.sec_permission sp,
    ${ohdsiSchema}.sec_role sr
  WHERE sp.value = 'cdmresults:*:*:get'
        AND sr.name IN ('Atlas users');