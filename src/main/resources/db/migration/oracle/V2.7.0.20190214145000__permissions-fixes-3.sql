ALTER TABLE ${ohdsiSchema}.sec_permission ADD for_role_id INTEGER;

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, for_role_id)
SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, REPLACE(new_perms.val, '%s', REPLACE(REPLACE(value, 'source:', ''), ':access', '')), role_id
FROM ${ohdsiSchema}.sec_permission sp
  JOIN ${ohdsiSchema}.sec_role_permission srp on sp.id = srp.permission_id
  CROSS JOIN (
    SELECT 'vocabulary:%s:concept:*:get' val FROM DUAL
    UNION ALL
    SELECT 'vocabulary:%s:concept:*:related:get' FROM DUAL
    UNION ALL
    SELECT 'cohortdefinition:*:cancel:%s:get' FROM DUAL
    UNION ALL
    SELECT 'featureextraction:query:prevalence:*:%s:get' FROM DUAL
    UNION ALL
    SELECT 'featureextraction:query:distributions:*:%s:get' FROM DUAL
    UNION ALL
    SELECT 'featureextraction:explore:prevalence:*:%s:*:get' FROM DUAL
    UNION ALL
    SELECT 'featureextraction:generate:%s:*:get' FROM DUAL
    UNION ALL
    SELECT 'featureextraction:generatesql:%s:*:get' FROM DUAL
  ) new_perms
WHERE sp.value LIKE 'source:%:access';

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sp.for_role_id, sp.id
FROM ${ohdsiSchema}.sec_permission sp
WHERE sp.for_role_id IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.sec_permission DROP COLUMN for_role_id;