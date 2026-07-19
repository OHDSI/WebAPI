INSERT INTO  ${ohdsiSchema}.sec_permission (id, value, description)
SELECT nextval('${ohdsiSchema}.sec_permission_id_seq'), 'vocabulary:*:compare-diff-vocab:post', 'Concept sets comparison permission (compare over different vocabularies method)'
WHERE NOT EXISTS (
        SELECT NULL FROM  ${ohdsiSchema}.sec_permission
        WHERE value = 'vocabulary:*:compare-diff-vocab:post'
);

INSERT INTO  ${ohdsiSchema}.sec_permission (id, value, description)
SELECT nextval('${ohdsiSchema}.sec_permission_id_seq'), 'vocabulary:*:compare-arbitrary-diff-vocab:post', 'Concept sets comparison permission (compare-arbitrary over different vocabularies method)'
WHERE NOT EXISTS (
        SELECT NULL FROM  ${ohdsiSchema}.sec_permission
        WHERE value = 'vocabulary:*:compare-arbitrary-diff-vocab:post'
);

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
    'vocabulary:*:compare-diff-vocab:post',
    'vocabulary:*:compare-arbitrary-diff-vocab:post'
    ) AND sr.name IN ('Atlas users')
  AND NOT EXISTS (
        SELECT NULL FROM ${ohdsiSchema}.sec_role_permission
        WHERE permission_id = sp.id and role_id = sr.id);


