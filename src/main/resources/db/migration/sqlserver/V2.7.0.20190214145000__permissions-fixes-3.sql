ALTER TABLE ${ohdsiSchema}.sec_permission ADD for_role_id INTEGER;
GO

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, for_role_id)
SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, REPLACE(new_perms.val, '%s', REPLACE(REPLACE(value, 'source:', ''), ':access', '')), role_id
FROM ${ohdsiSchema}.sec_permission sp
  JOIN ${ohdsiSchema}.sec_role_permission srp on sp.id = srp.permission_id
  CROSS JOIN (
    SELECT 'vocabulary:%s:concept:*:get' val
    UNION ALL
    SELECT 'vocabulary:%s:concept:*:related:get'
    UNION ALL
    SELECT 'cohortdefinition:*:cancel:%s:get'
    UNION ALL
    SELECT 'featureextraction:query:prevalence:*:%s:get'
    UNION ALL
    SELECT 'featureextraction:query:distributions:*:%s:get'
    UNION ALL
    SELECT 'featureextraction:explore:prevalence:*:%s:*:get'
    UNION ALL
    SELECT 'featureextraction:generate:%s:*:get'
    UNION ALL
    SELECT 'featureextraction:generatesql:%s:*:get'
  ) new_perms
WHERE sp.value LIKE 'source:%:access';

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, sp.for_role_id, sp.id
FROM ${ohdsiSchema}.sec_permission sp
WHERE sp.for_role_id IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.sec_permission DROP COLUMN for_role_id;