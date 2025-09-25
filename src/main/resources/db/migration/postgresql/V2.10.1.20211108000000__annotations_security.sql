INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
VALUES (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'annotation:get', 'Gets annotation summary'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'annotation:*:get', 'Get annotation result by ID.'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'annotation:fullquestion:get', 'Get annotation result by ID.'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'annotation:post', 'Adds an annotation result.'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'annotation:sample:post', 'Adds an annotation to sample.'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'annotation:answers:post', 'Adds an annotation to sample.'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'annotation:navigation:get', 'Gets annotation navigation.'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'annotation:questions:get', 'Gets annotation questions.'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'annotation:questions:post', 'Adds question to annotation.'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'annotation:results:get', 'Gets annotation results.'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'annotation:results:*:get', 'Gets annotation results by ID.'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'annotation:results:completeResults:get', 'Gets complete annotation results.'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'annotation:sets:get', 'Gets annotation sets.'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'annotation:sets:post', 'Create annotation sets.'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'annotation:getsets:get', 'Gets annotation sets as getsets.'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'annotation:deleteset:*:get', 'Deletes an annotation set.')
;

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP,
     ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
                   'annotation:get',
                   'annotation:*:get',
                   'annotation:fullquestion:get',
                   'annotation:post',
                   'annotation:sample:post',
                   'annotation:answers:post',
                   'annotation:navigation:get',
                   'annotation:questions:get',
                   'annotation:questions:post',
                   'annotation:results:get',
                   'annotation:results:*:get',
                   'annotation:sets:get',
                   'annotation:sets:post',
                   'annotation:getsets:get',
                   'annotation:deleteset:*:get')
  AND sr.name IN ('Atlas users');
  