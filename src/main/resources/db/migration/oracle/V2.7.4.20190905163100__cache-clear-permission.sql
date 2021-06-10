INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
    SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'cache:clear:get', 'Clear middle-tier caches' FROM dual;

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
    SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sr.id, sp.id
    FROM (SELECT id FROM ${ohdsiSchema}.sec_permission WHERE value = 'cache:clear:get') sp
      CROSS JOIN (SELECT id FROM ${ohdsiSchema}.sec_role WHERE name = 'admin' AND system_role = 1) sr;