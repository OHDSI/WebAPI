-- cohort-characterizations permissions

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
    SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, REPLACE(value, ':post', ':delete'),
      'Cancel Generation of Cohort Characterization with ID = ' + REPLACE(REPLACE(value, 'cohort-characterization:', ''), ':generation:*:post', '')
    FROM ${ohdsiSchema}.sec_permission sp
      JOIN ${ohdsiSchema}.SEC_ROLE_PERMISSION srp on srp.PERMISSION_ID = sp.ID
    WHERE sp.VALUE like 'cohort-characterization:%:generation:*:post' AND NOT sp.value = 'cohort-characterization:*:generation:*:post';

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
  select srp.role_id, spc.id as permission_id from ${ohdsiSchema}.SEC_PERMISSION sp
    join ${ohdsiSchema}.SEC_ROLE_PERMISSION srp on srp.PERMISSION_ID = sp.ID
    join ${ohdsiSchema}.SEC_PERMISSION spc ON replace(replace(sp.value, 'cohort-characterization:', ''),':generation:*:post', '') = replace(replace(spc.value, 'cohort-characterization:', ''),':generation:*:delete', '')
    and spc.value like 'cohort-characterization:%:generation:*:delete'
  where sp.VALUE like 'cohort-characterization:%:generation:*:post';

-- pathways permissions

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
  SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, REPLACE(value, ':post', ':delete'),
    'Cancel Generation of Pathway Analysis with ID = ' + REPLACE(REPLACE(value, 'cohort-characterization:', ''), ':generation:*:post', '')
  FROM ${ohdsiSchema}.sec_permission sp
    JOIN ${ohdsiSchema}.SEC_ROLE_PERMISSION srp on srp.PERMISSION_ID = sp.ID
  WHERE sp.VALUE like 'pathway-analysis:%:generation:*:post' AND NOT sp.value = 'pathway-analysis:*:generation:*:post';

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
  select srp.role_id, spc.id as permission_id from ${ohdsiSchema}.SEC_PERMISSION sp
    join ${ohdsiSchema}.SEC_ROLE_PERMISSION srp on srp.PERMISSION_ID = sp.ID
    join ${ohdsiSchema}.SEC_PERMISSION spc ON replace(replace(sp.value, 'pathway-analysis:', ''),':generation:*:post', '') = replace(replace(spc.value, 'pathway-analysis:', ''),':generation:*:delete', '')
                                              and spc.value like 'pathway-analysis:%:generation:*:delete'
  where sp.VALUE like 'pathway-analysis:%:generation:*:post';