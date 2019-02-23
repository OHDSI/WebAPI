DELETE
FROM ${ohdsiSchema}.sec_role_permission
WHERE permission_id IN (SELECT id FROM ${ohdsiSchema}.sec_permission WHERE value NOT IN ('user:me:get'))
AND role_id = (SELECT id FROM ${ohdsiSchema}.sec_role WHERE name = 'public');

DELETE
FROM ${ohdsiSchema}.sec_role_permission
WHERE permission_id IN (SELECT id FROM ${ohdsiSchema}.sec_permission WHERE value IN ('ir:*:execute:*:get', 'ir:*:execute:*:delete', '*:person:*:get'))
AND role_id = (SELECT id FROM ${ohdsiSchema}.sec_role WHERE name = 'Atlas users');

ALTER TABLE ${ohdsiSchema}.sec_permission ADD for_role_id INTEGER;

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, for_role_id)
SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval,
  REPLACE(CAST(new_perms.val AS VARCHAR2(255)), '%s', REPLACE(REPLACE(value, 'cohortdefinition:*:generate:', ''), ':get', '')), role_id
FROM ${ohdsiSchema}.sec_permission sp
  JOIN ${ohdsiSchema}.sec_role_permission srp on sp.id = srp.permission_id
  CROSS JOIN (
    SELECT 'ir:*:execute:%s:get' val FROM DUAL
    UNION ALL
    SELECT 'ir:*:execute:%s:delete' FROM DUAL
    UNION ALL
    SELECT '%s:person:*:get' FROM DUAL
  ) new_perms
WHERE sp.value LIKE 'cohortdefinition:*:generate:%:get' AND sp.value <> 'cohortdefinition:*:generate:*:get';

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sp.for_role_id, sp.id
FROM ${ohdsiSchema}.sec_permission sp
WHERE sp.for_role_id IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.sec_permission DROP COLUMN for_role_id;

-- Allow Atlas users to see list of sources
INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'configuration:edit:ui'
  )
  AND sr.name IN ('Atlas users');