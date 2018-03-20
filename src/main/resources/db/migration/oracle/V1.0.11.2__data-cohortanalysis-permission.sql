CREATE SEQUENCE ${ohdsiSchema}.sec_permission_id_seq START WITH 500;

insert into ${ohdsiSchema}.sec_permission (id, value, description)
values (${ohdsiSchema}.sec_permission_id_seq.nextval, 'cohortanalysis:post', 'Create Cohort analyses');

insert into ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT ${ohdsiSchema}.sec_role_permission_sequence.NEXTVAL, sr.id, sp.id
    FROM ${ohdsiSchema}.sec_permission sp,
      ${ohdsiSchema}.sec_role sr
    WHERE sp.VALUE = 'cohortanalysis:post'
      AND sr.NAME IN ('admin', 'cohort creator', 'Atlas users');