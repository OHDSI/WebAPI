INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
values (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'cdmresults:*:*:post', 'View CDM results');

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
values (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'cohort:*:get', 'View cohort results');

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
values (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'cohort:*:*:get', 'View cohort results');

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
values (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'cohort:import:post', 'Import cohort results');

INSERT INTO ${ohdsiSchema}.sec_role_permission (role_id, permission_id)
  SELECT
    sr.id,
    sp.id
  FROM ${ohdsiSchema}.sec_permission sp,
    ${ohdsiSchema}.sec_role sr
  WHERE sp."value" IN ('cdmresults:*:*:post', 'cohort:*:get', 'cohort:*:*:get', 'cohort:import:post')
        AND sr.name IN ('Atlas users');