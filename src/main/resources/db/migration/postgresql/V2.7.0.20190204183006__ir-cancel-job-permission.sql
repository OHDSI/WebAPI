INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
    VALUES (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'ir:*:execute:*:delete', 'Cancel IR analysis execution');

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
  SELECT sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'ir:*:execute:*:delete'
  )
        AND sr.name IN ('Atlas users');
