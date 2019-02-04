INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
    VALUES (${ohdsiSchema}.sec_permission_id_seq.nextval, 'ir:*:execute:*:cancel:get', 'Cancel IR analysis execution');

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
  SELECT sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'ir:*:execute:*:cancel:get'
  )
        AND sr.name IN ('Atlas users');
