-- Remap permissions from 'Atlas users' to 'admin' role

-- Remove existing role_permission mappings for 'Atlas users'
DELETE FROM ${ohdsiSchema}.sec_role_permission
WHERE permission_id IN (
    SELECT id FROM ${ohdsiSchema}.sec_permission 
    WHERE value IN (
        'job:*:artifact:get',
        'conceptset:check-filter-count:post',
        'conceptset:compare-batch:post'
    )
)
AND role_id IN (
    SELECT id FROM ${ohdsiSchema}.sec_role WHERE name = 'Atlas users'
);

-- Add role_permission mappings for 'admin' role
INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
    'job:*:artifact:get',
    'conceptset:check-filter-count:post',
    'conceptset:compare-batch:post'
)
AND sr.name = 'admin'
AND NOT EXISTS (
    SELECT NULL FROM ${ohdsiSchema}.sec_role_permission
    WHERE permission_id = sp.id AND role_id = sr.id
);