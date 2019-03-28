ALTER TABLE ${ohdsiSchema}.sec_permission ADD role_id_tmp INTEGER;
WITH perms_for_all_sources (id, value, description, role_id) AS (
  SELECT nextval('${ohdsiSchema}.sec_permission_id_seq'),
         REPLACE(new_perms.val, '%s', REPLACE(REPLACE(value, 'source:', ''), ':access', '')),
         REPLACE(new_perms.descr, '%s', REPLACE(REPLACE(value, 'source:', ''), ':access', '')),
         role_id
  FROM ${ohdsiSchema}.sec_permission sp
         JOIN ${ohdsiSchema}.sec_role_permission srp on sp.id = srp.permission_id
         CROSS JOIN (
    SELECT 'ir:*:report:%s:get'::text val, 'Get IR generation report with SourceKey = %s'::text descr
  ) new_perms
  WHERE sp.value LIKE 'source:%:access'
)
INSERT
INTO ${ohdsiSchema}.sec_permission (id, value, description, role_id_tmp)
SELECT perms_for_all_sources.id,
       perms_for_all_sources.value,
       perms_for_all_sources.description,
       perms_for_all_sources.role_id
FROM perms_for_all_sources EXCEPT
SELECT perms_for_all_sources.id,
       perms_for_all_sources.value,
       perms_for_all_sources.description,
       perms_for_all_sources.role_id
FROM perms_for_all_sources
       JOIN ${ohdsiSchema}.sec_permission ON perms_for_all_sources.value = ${ohdsiSchema}.sec_permission.value;

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sp.role_id_tmp, sp.id
FROM ${ohdsiSchema}.sec_permission sp
WHERE sp.role_id_tmp IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.sec_permission
  DROP COLUMN role_id_tmp;