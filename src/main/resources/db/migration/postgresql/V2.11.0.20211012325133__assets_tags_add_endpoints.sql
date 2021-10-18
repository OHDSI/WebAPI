INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
VALUES (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'cohortdefinition:byTag:post',
        'Get cohort definitions with certain assigned tags'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:byTag:post',
        'Get concept sets with certain assigned tags'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'cohort-characterization:byTag:post',
        'Get cohort characterizations with certain assigned tags'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'ir:byTag:post',
        'Get incidence rates with certain assigned tags'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'pathway-analysis:byTag:post',
        'Get pathways with certain assigned tags'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'reusable:byTag:post',
        'Get reusables with certain assigned tags');

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP,
     ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
                   'cohortdefinition:byTag:post',
                   'conceptset:byTag:post',
                   'cohort-characterization:byTag:post',
                   'ir:byTag:post',
                   'pathway-analysis:byTag:post',
                   'reusable:byTag:post')
  AND sr.name IN ('Atlas users');
