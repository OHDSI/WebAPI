ALTER TABLE ${ohdsiSchema}.sec_permission ADD for_role_id INTEGER;

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, for_role_id)
SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, REPLACE(new_perms.val, '%s', REPLACE(REPLACE(value, 'source:', ''), ':access', '')), role_id
FROM ${ohdsiSchema}.sec_permission sp
  JOIN ${ohdsiSchema}.sec_role_permission srp on sp.id = srp.permission_id
  CROSS JOIN (
    SELECT 'vocabulary:%s:*:get' val FROM DUAL
    UNION ALL
    SELECT 'vocabulary:%s:included-concepts:count:post' FROM DUAL
    UNION ALL
    SELECT 'vocabulary:%s:resolveConceptSetExpression:post' FROM DUAL
    UNION ALL
    SELECT 'vocabulary:%s:lookup:identifiers:post' FROM DUAL
    UNION ALL
    SELECT 'vocabulary:%s:lookup:identifiers:ancestors:post' FROM DUAL
    UNION ALL
    SELECT 'vocabulary:%s:lookup:mapped:post' FROM DUAL
    UNION ALL
    SELECT 'vocabulary:%s:compare:post' FROM DUAL
    UNION ALL
    SELECT 'vocabulary:%s:optimize:post' FROM DUAL
    UNION ALL
    SELECT 'cdmresults:%s:*:get' FROM DUAL
    UNION ALL
    SELECT 'cdmresults:%s:*:*:get' FROM DUAL
    UNION ALL
    SELECT 'cdmresults:%s:conceptRecordCount:post' FROM DUAL
    UNION ALL
    SELECT 'cohortresults:%s:*:*:get' FROM DUAL
    UNION ALL
    SELECT 'cohortresults:%s:*:*:*:get' FROM DUAL
  ) new_perms
WHERE sp.value LIKE 'source:%:access';

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sp.for_role_id, sp.id
FROM ${ohdsiSchema}.sec_permission sp
WHERE sp.for_role_id IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.sec_permission DROP COLUMN for_role_id;

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (${ohdsiSchema}.sec_permission_id_seq.nextval, 'conceptset:*:expression:get', 'Resolve concept set expression');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (${ohdsiSchema}.sec_permission_id_seq.nextval, 'conceptset:*:generationinfo:get', 'Get generation info for concept set');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (${ohdsiSchema}.sec_permission_id_seq.nextval, 'cohortdefinition:*:check:get', 'Get cohort definition design checks');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (${ohdsiSchema}.sec_permission_id_seq.nextval, 'sqlrender:translate:post' , 'Translate SQL');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (${ohdsiSchema}.sec_permission_id_seq.nextval, 'ir:*:info' , 'Get IR info');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (${ohdsiSchema}.sec_permission_id_seq.nextval, 'job:type:*:name:*:get' , 'Get IR info');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sr.id, sp.id
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