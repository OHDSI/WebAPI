INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
VALUES (${ohdsiSchema}.sec_permission_id_seq.nextval, 'cdmresults:*:*:post', 'View CDM results');

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
SELECT
    ${ohdsiSchema}.sec_permission_id_seq.nextval,
    'cohort:*:get',
    'View cohort results';

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
SELECT
    ${ohdsiSchema}.sec_permission_id_seq.nextval,
    'cohort:*:*:get',
    'View cohort results';

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
SELECT
    ${ohdsiSchema}.sec_permission_id_seq.nextval,
    'cohort:import:post',
    'Import cohort results';

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
  SELECT
    ${ohdsiSchema}.sec_role_permission_sequence.NEXTVAL,
    sr.id,
    sp.id
  FROM ${ohdsiSchema}.sec_permission sp,
    ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN ('cdmresults:*:*:post', 'cohort:*:get', 'cohort:*:*:get', 'cohort:import:post')
        AND sr.name IN ('Atlas users');