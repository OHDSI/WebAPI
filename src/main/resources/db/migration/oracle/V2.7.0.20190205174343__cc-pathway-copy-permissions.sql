-- cc copy permissions

INSERT INTO ${ohdsiSchema}.SEC_PERMISSION(id, value, description)
    VALUES (${ohdsiSchema}.sec_permission_id_seq.nextval, 'cohort-characterization:*:post', '');

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
  SELECT sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'cohort-characterization:*:post'
  ) AND sr.name IN ('Atlas users');

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
  SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, REPLACE(value, ':delete', ':post'),
    'Create a copy of Cohort Characterization with ID = ' || REPLACE(REPLACE(value, 'cohort-characterization:'), ':delete')
  FROM ohdsi.SEC_PERMISSION sp
    JOIN ohdsi.SEC_ROLE_PERMISSION srp on srp.PERMISSION_ID = sp.ID
  WHERE sp.VALUE like 'cohort-characterization:%:delete';

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
  select srp.role_id, spc.id as permission_id from ${ohdsiSchema}.SEC_PERMISSION sp
    join ${ohdsiSchema}.SEC_ROLE_PERMISSION srp on srp.PERMISSION_ID = sp.ID
    join ${ohdsiSchema}.SEC_PERMISSION spc ON REPLACE(REPLACE(sp.value, 'cohort-characterization:'), ':post') = REPLACE(REPLACE(spc.value, 'cohort-characterization:'), ':delete')
                                              and spc.value like 'cohort-characterization:%:post'
  where sp.VALUE like 'cohort-characterization:%:post';

-- pathway copy permissions

INSERT INTO ${ohdsiSchema}.SEC_PERMISSION(id, value, description)
VALUES (${ohdsiSchema}.sec_permission_id_seq.nextval, 'pathway-analysis:*:post', '');

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
  SELECT sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'pathway-analysis:*:post'
  ) AND sr.name IN ('Atlas users');

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
  SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, REPLACE(value, ':delete', ':post'),
    'Create a copy of Pathway Analysis with ID = ' + REPLACE(REPLACE(value, 'pathway-analysis:'), ':delete')
  FROM ohdsi.SEC_PERMISSION sp
    JOIN ohdsi.SEC_ROLE_PERMISSION srp on srp.PERMISSION_ID = sp.ID
  WHERE sp.VALUE like 'pathway-analysis:%:delete';

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
  select srp.role_id, spc.id as permission_id from ${ohdsiSchema}.SEC_PERMISSION sp
    join ${ohdsiSchema}.SEC_ROLE_PERMISSION srp on srp.PERMISSION_ID = sp.ID
    join ${ohdsiSchema}.SEC_PERMISSION spc ON REPLACE(REPLACE(sp.value, 'pathway-analysis:'), ':post') = REPLACE(REPLACE(spc.value, 'pathway-analysis:'), ':delete')
                                              and spc.value like 'pathway-analysis:%:post'
  where sp.VALUE like 'pathway-analysis:%:post';
