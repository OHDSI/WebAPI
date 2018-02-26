CREATE SEQUENCE ${ohdsiSchema}.sec_permission_id_seq START WITH 500;
ALTER TABLE ${ohdsiSchema}.sec_permission ADD CONSTRAINT permission_unique UNIQUE (value);

insert into ${ohdsiSchema}.sec_permission (id, value, description)
values (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'cohortanalysis:post', 'Create Cohort analyses');

ALTER TABLE ${ohdsiSchema}.sec_role_permission ADD CONSTRAINT role_permission_unique UNIQUE (role_id, permission_id);

insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
    FROM ${ohdsiSchema}.sec_permission sp,
      ${ohdsiSchema}.sec_role sr
    WHERE sp."value" = 'cohortanalysis:post'
      AND sr.name IN ('admin', 'cohort creator', 'Atlas users');