INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (${ohdsiSchema}.sec_permission_id_seq.nextval, 'conceptset:*:copy-name:get', 'Get name for copying concept set');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
WHERE sp.value = 'conceptset:*:copy-name:get'
AND sr.name IN ('concept set creator', 'Moderator', 'Atlas users');