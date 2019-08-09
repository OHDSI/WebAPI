INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
    SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'user:runas:post', 'Sign in as another user' FROM dual;

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
    SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sr.id, sp.id
    FROM (SELECT id FROM ${ohdsiSchema}.sec_permission WHERE value = 'user:runas:post') sp
      CROSS JOIN (SELECT id FROM ${ohdsiSchema}.sec_role WHERE name = 'admin' AND system_role = 1) sr;