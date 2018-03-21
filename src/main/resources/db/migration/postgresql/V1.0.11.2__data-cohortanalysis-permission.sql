CREATE SEQUENCE IF NOT EXISTS ${ohdsiSchema}.sec_permission_id_seq
  START WITH 500;

ALTER TABLE ${ohdsiSchema}.sec_permission ADD CONSTRAINT permission_unique UNIQUE (value);

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
  SELECT
    nextval('${ohdsiSchema}.sec_permission_id_seq'),
    'cohortanalysis:post',
    'Create Cohort analyses'
  WHERE NOT EXISTS(SELECT 1
                   FROM ${ohdsiSchema}.sec_permission
                   WHERE value = 'cohortanalysis:post'
  );

ALTER TABLE ${ohdsiSchema}.sec_role_permission ADD CONSTRAINT role_permission_unique UNIQUE (role_id, permission_id);

INSERT INTO ${ohdsiSchema}.sec_role_permission (role_id, permission_id)
  SELECT
    sr.id,
    sp.id
  FROM ${ohdsiSchema}.sec_permission sp,
    ${ohdsiSchema}.sec_role sr
  WHERE sp."value" = 'cohortanalysis:post'
        AND sr.name IN ('admin', 'cohort creator', 'Atlas users')
        AND NOT EXISTS(SELECT 1
                       FROM ${ohdsiSchema}.sec_role_permission
                       WHERE role_id = sr.id AND permission_id = sp.id
  );