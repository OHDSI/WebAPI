INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
(nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohortsample:*:*:get', 'List cohort samples'),
(nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohortsample:*:*:*:get', 'Get single cohort samples'),
(nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohortsample:*:*:*:delete', 'Delete cohort sample'),
(nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohortsample:*:*:delete', 'Delete all cohort samples of a cohort.'),
(nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohortsample:*:*:post', 'Create cohort sample'),
(nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohortsample:*:*:*:refresh:post', 'Refresh cohort sample');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
        'cohortsample:*:*:get',
        'cohortsample:*:*:*:get',
        'cohortsample:*:*:*:delete',
        'cohortsample:*:*:delete',
        'cohortsample:*:*:post',
        'cohortsample:*:*:*:refresh:post'
    ) AND sr.name IN ('Atlas users');
