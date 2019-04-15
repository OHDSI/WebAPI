ALTER TABLE ${ohdsiSchema}.sec_permission ADD role_id_tmp INTEGER;

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description, role_id_tmp)
  SELECT nextval('${ohdsiSchema}.sec_permission_id_seq'), REPLACE(CAST(new_perms.val AS VARCHAR), '%s', REPLACE(REPLACE(sp.value, 'source:', ''), ':access', '')),
    REPLACE(CAST(new_perms.descr AS VARCHAR), '%s', REPLACE(REPLACE(sp.value, 'source:', ''), ':access', '')) description, srp.role_id
  FROM ${ohdsiSchema}.sec_permission sp
  JOIN ${ohdsiSchema}.sec_role_permission srp on sp.id = srp.permission_id
    CROSS JOIN (
        SELECT 'ir:*:report:%s:get' val, 'Get IR generation report with SourceKey = %s' descr
    ) new_perms
  WHERE sp.value LIKE 'source:%:access'
      AND NOT EXISTS(SELECT tsp.id FROM ${ohdsiSchema}.sec_permission tsp JOIN ${ohdsiSchema}.sec_role_permission tsrp ON tsrp.permission_id = tsp.id
      AND tsp.value = REPLACE(CAST(new_perms.val AS VARCHAR), '%s', REPLACE(REPLACE(sp.value, 'source:', ''), ':access', '')));

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sp.role_id_tmp, sp.id
FROM ${ohdsiSchema}.sec_permission sp
WHERE sp.role_id_tmp IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.sec_permission
  DROP COLUMN role_id_tmp;