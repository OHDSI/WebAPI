delete from ${ohdsiSchema}.sec_role_permission where
  permission_id in (select id from ${ohdsiSchema}.sec_permission where
    value like 'pathway-analysis:%:generation:*:post' or
    value like 'pathway-analysis:%:generation:*:delete'
  );
delete from ${ohdsiSchema}.sec_permission where
  value like 'pathway-analysis:%:generation:*:post' or
  value like 'pathway-analysis:%:generation:*:delete';

alter table ${ohdsiSchema}.sec_permission add for_role_id int;

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, for_role_id)
  SELECT nextval('${ohdsiSchema}.sec_permission_id_seq'), REPLACE(CAST(new_perms.val AS VARCHAR(255)), '%s', REPLACE(REPLACE(value, 'source:', ''), ':access', '')), role_id
FROM ${ohdsiSchema}.sec_permission sp
JOIN ${ohdsiSchema}.sec_role_permission srp on sp.id = srp.permission_id
CROSS JOIN (
SELECT 'pathway-analysis:*:generation:%s:post' val UNION ALL
SELECT 'pathway-analysis:*:generation:%s:delete' val
) new_perms
WHERE sp.value LIKE 'source:%:access';

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
  SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sp.for_role_id, sp.id
FROM ${ohdsiSchema}.sec_permission sp
WHERE sp.for_role_id IS NOT NULL;

alter table ${ohdsiSchema}.sec_permission drop column for_role_id;