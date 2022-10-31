CREATE TEMP TABLE temp_migration (
  from_perm_id int,
  new_value character varying(255)
);

INSERT INTO temp_migration (from_perm_id, new_value)
SELECT sp.id as from_id,
  REPLACE(CAST(new_perms.val AS VARCHAR(255)), '%s', REPLACE(REPLACE(value, 'source:', ''), ':access', '')) as new_value
FROM ${ohdsiSchema}.sec_permission sp
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

INSERT INTO ${ohdsiSchema}.sec_permission (id, value)
SELECT nextval('${ohdsiSchema}.sec_permission_id_seq'), new_value
FROM temp_migration;

INSERT INTO ${ohdsiSchema}.sec_role_permission (id,role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'),
  srp.role_id,
  sp.id as permission_id
FROM temp_migration m
JOIN ${ohdsiSchema}.sec_permission sp on m.new_value = sp.value
JOIN ${ohdsiSchema}.sec_role_permission srp on m.from_perm_id = srp.permission_id;

drop table temp_migration;

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
    VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:*:expression:get', 'Resolve concept set expression'),
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:*:generationinfo:get', 'Get generation info for concept set'),
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohortdefinition:*:check:get', 'Get cohort definition design checks'),
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'sqlrender:translate:post' , 'Translate SQL'),
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'ir:*:info' , 'Get IR info'),
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'job:type:*:name:*:get' , 'Get IR info');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
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