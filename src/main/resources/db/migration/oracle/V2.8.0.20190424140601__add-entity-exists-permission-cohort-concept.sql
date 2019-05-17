INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
WHERE sp.value = 'conceptset:*:exists:get'
AND sr.name = 'Atlas users';

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
WHERE sp.value = 'cohortdefinition:*:exists:get'
AND sr.name = 'Atlas users';