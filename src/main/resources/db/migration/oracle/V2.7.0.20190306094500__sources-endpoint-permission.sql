INSERT INTO ${ohdsiSchema}.sec_role_permission (id, permission_id, role_id)
SELECT
  ${ohdsiSchema}.sec_role_permission_sequence.nextval,
  (select id from ${ohdsiSchema}.sec_permission where value in ('source:*:get')) permission_id,
  (SELECT id FROM ${ohdsiSchema}.sec_role WHERE name = 'public') role_id
FROM DUAL;