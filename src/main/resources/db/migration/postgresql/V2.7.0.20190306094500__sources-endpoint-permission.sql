INSERT INTO ${ohdsiSchema}.sec_role_permission (id, permission_id, role_id)
SELECT
  nextval('${ohdsiSchema}.sec_role_permission_sequence'),
  (select id from ${ohdsiSchema}.sec_permission where value in ('source:*:get')) permission_id,
  (SELECT id FROM ${ohdsiSchema}.sec_role WHERE name = 'public') role_id;