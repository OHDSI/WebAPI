ALTER TABLE ${ohdsiSchema}.sec_permission ADD for_role_id INTEGER;
GO

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, for_role_id)
SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, REPLACE(new_perms.val, '%s', REPLACE(REPLACE(value, 'source:', ''), ':access', '')), role_id
FROM ${ohdsiSchema}.sec_permission sp
  JOIN ${ohdsiSchema}.sec_role_permission srp on sp.id = srp.permission_id
  CROSS JOIN (
    SELECT 'vocabulary:%s:*:get' val
    UNION ALL
    SELECT 'vocabulary:%s:included-concepts:count:post'
    UNION ALL
    SELECT 'vocabulary:%s:resolveConceptSetExpression:post'
    UNION ALL
    SELECT 'vocabulary:%s:lookup:identifiers:post'
    UNION ALL
    SELECT 'vocabulary:%s:lookup:identifiers:ancestors:post'
    UNION ALL
    SELECT 'vocabulary:%s:lookup:mapped:post'
    UNION ALL
    SELECT 'vocabulary:%s:compare:post'
    UNION ALL
    SELECT 'vocabulary:%s:optimize:post'
    UNION ALL
    SELECT 'cdmresults:%s:*:get'
    UNION ALL
    SELECT 'cdmresults:%s:*:*:get'
    UNION ALL
    SELECT 'cdmresults:%s:conceptRecordCount:post'
    UNION ALL
    SELECT 'cohortresults:%s:*:*:get'
    UNION ALL
    SELECT 'cohortresults:%s:*:*:*:get'
  ) new_perms
WHERE sp.value LIKE 'source:%:access';

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, sp.for_role_id, sp.id
FROM ${ohdsiSchema}.sec_permission sp
WHERE sp.for_role_id IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.sec_permission DROP COLUMN for_role_id;

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
    VALUES
    (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'conceptset:*:expression:get', 'Resolve concept set expression'),
    (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'conceptset:*:generationinfo:get', 'Get generation info for concept set'),
    (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'cohortdefinition:*:check:get', 'Get cohort definition design checks'),
    (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'sqlrender:translate:post' , 'Translate SQL'),
    (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'ir:*:info' , 'Get IR info'),
    (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'job:type:*:name:*:get' , 'Get IR info');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'conceptset:*:expression:get',
    'conceptset:*:generationinfo:get',
    'cohortdefinition:*:check:get',
    'sqlrender:translate:post',
    'ir:*:info',
    'job:type:*:name:*:get'
  )
  AND sr.name IN ('Atlas users');