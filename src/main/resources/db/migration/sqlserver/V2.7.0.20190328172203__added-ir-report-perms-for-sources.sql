ALTER TABLE ${ohdsiSchema}.sec_permission
  ADD role_id_tmp INTEGER;
GO

CREATE TABLE #Seq_values
(
  sequence_value   INT,
  permission_value VARCHAR(50)
);
GO

INSERT INTO #Seq_values
SELECT 0, REPLACE(new_perms.val, '%s', REPLACE(REPLACE(value, 'source:', ''), ':access', ''))
FROM ${ohdsiSchema}.sec_permission sp
       JOIN ${ohdsiSchema}.sec_role_permission srp on sp.id = srp.permission_id
       CROSS JOIN (
  SELECT 'ir:*:report:%s:get' val
) new_perms
WHERE sp.value LIKE 'source:%:access';

UPDATE #Seq_values
SET sequence_value = NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq
WHERE #Seq_values.sequence_value = 0;

WITH perms_for_all_sources (id, value, description, role_id) AS (
  SELECT sequence_value,
         REPLACE(new_perms.val, '%s', REPLACE(REPLACE(value, 'source:', ''), ':access', '')),
         REPLACE(new_perms.descr, '%s', REPLACE(REPLACE(value, 'source:', ''), ':access', '')),
         role_id
  FROM ${ohdsiSchema}.sec_permission sp
         JOIN ${ohdsiSchema}.sec_role_permission srp on sp.id = srp.permission_id
         CROSS JOIN (
    SELECT 'ir:*:report:%s:get' val, 'Get IR generation report with SourceKey = %s' descr
  ) new_perms
         JOIN #Seq_values ON val = #Seq_values.permission_value
  WHERE sp.value LIKE 'source:%:access'
)
INSERT
INTO ${ohdsiSchema}.sec_permission (id, value, description, role_id_tmp)
SELECT perms_for_all_sources.id,
       perms_for_all_sources.value,
       perms_for_all_sources.description,
       perms_for_all_sources.role_id
FROM perms_for_all_sources EXCEPT
SELECT perms_for_all_sources.id,
       perms_for_all_sources.value,
       perms_for_all_sources.description,
       perms_for_all_sources.role_id
FROM perms_for_all_sources
       JOIN ${ohdsiSchema}.sec_permission ON perms_for_all_sources.value = ${ohdsiSchema}.sec_permission.value;

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, sp.role_id_tmp, sp.id
FROM ${ohdsiSchema}.sec_permission sp
WHERE sp.role_id_tmp IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.sec_permission
  DROP COLUMN role_id_tmp;

DROP TABLE #Seq_values;