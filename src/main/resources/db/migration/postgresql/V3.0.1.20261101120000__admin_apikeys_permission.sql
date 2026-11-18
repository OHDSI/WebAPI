INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
SELECT nextval('${ohdsiSchema}.sec_permission_sequence'), 'admin:apikeys', 'View and revoke any user''s API keys'
WHERE NOT EXISTS (SELECT 1 FROM ${ohdsiSchema}.sec_permission WHERE value = 'admin:apikeys');

-- Databases upgraded from 2.x give the admin role an explicit permission list rather than '*'.
INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), r.id, p.id
FROM ${ohdsiSchema}.sec_role r
JOIN ${ohdsiSchema}.sec_permission p ON p.value = 'admin:apikeys'
WHERE r.name = 'admin'
  AND NOT EXISTS (
    SELECT 1 FROM ${ohdsiSchema}.sec_role_permission rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
