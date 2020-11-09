INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
(${ohdsiSchema}.sec_permission_id_seq.nextval, 'cohortsample:*:*:get', 'List cohort samples'),
(${ohdsiSchema}.sec_permission_id_seq.nextval, 'cohortsample:*:*:*:get', 'Get single cohort samples'),
(${ohdsiSchema}.sec_permission_id_seq.nextval, 'cohortsample:*:*:*:delete', 'Delete cohort sample'),
(${ohdsiSchema}.sec_permission_id_seq.nextval, 'cohortsample:*:*:delete', 'Delete all cohort samples of a cohort.'),
(${ohdsiSchema}.sec_permission_id_seq.nextval, 'cohortsample:*:*:post', 'Create cohort sample')
(${ohdsiSchema}.sec_permission_id_seq.nextval, 'cohortsample:*:*:*:refresh:post', 'Refresh cohort sample');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
        'cohortsample:*:*:get',
        'cohortsample:*:*:*:get',
        'cohortsample:*:*:*:delete',
        'cohortsample:*:*:delete',
        'cohortsample:*:*:post',
        'cohortsample:*:*:*:refresh:post'
    ) AND sr.name IN ('Atlas users');
