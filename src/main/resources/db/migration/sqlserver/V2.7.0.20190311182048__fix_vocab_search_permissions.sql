delete from ${ohdsiSchema}.sec_role_permission
  where permission_id in (select id from ${ohdsiSchema}.sec_permission where value = 'vocabulary:*:search:*:get');

delete from ${ohdsiSchema}.sec_permission
  where value = 'vocabulary:*:search:*:get';

ALTER TABLE ${ohdsiSchema}.sec_permission ADD for_role_id INTEGER;
GO

insert into ${ohdsiSchema}.sec_permission(id, value, for_role_id)
SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, REPLACE(CAST(new_perms.val AS VARCHAR(255)), '%s', REPLACE(REPLACE(value, 'cohortdefinition:*:generate:', ''), ':get', '')), role_id
FROM ${ohdsiSchema}.sec_permission sp
JOIN ${ohdsiSchema}.sec_role_permission srp on sp.id = srp.permission_id
CROSS JOIN (
SELECT 'vocabulary:%s:search:*:get' val
) new_perms
WHERE sp.value LIKE 'cohortdefinition:*:generate:%:get';

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
  SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, sp.for_role_id, sp.id
FROM ${ohdsiSchema}.sec_permission sp
WHERE sp.for_role_id IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.sec_permission DROP COLUMN for_role_id;