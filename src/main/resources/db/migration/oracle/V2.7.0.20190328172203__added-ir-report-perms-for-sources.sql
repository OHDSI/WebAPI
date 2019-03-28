ALTER TABLE ${ohdsiSchema}.sec_permission
  ADD role_id_tmp INTEGER;

CREATE OR REPLACE FUNCTION ${ohdsiSchema}.get_next_sequence_val RETURN NUMBER IS
  seq_val NUMBER(19);
    BEGIN
      seq_val:= ${ohdsiSchema}.sec_permission_id_seq.nextval;
      RETURN seq_val;
    END get_next_sequence_val;
/

INSERT
INTO ${ohdsiSchema}.sec_permission (id, value, description, role_id_tmp)
WITH perms_for_all_sources (id, value, description, role_id) AS (
  SELECT ${ohdsiSchema}.get_next_sequence_val,
         REPLACE(new_perms.val, '%s', REPLACE(REPLACE(value, 'source:', ''), ':access', '')),
         REPLACE(new_perms.descr, '%s', REPLACE(REPLACE(value, 'source:', ''), ':access', '')),
         role_id
  FROM ${ohdsiSchema}.sec_permission sp
         JOIN ${ohdsiSchema}.sec_role_permission srp on sp.id = srp.permission_id
         CROSS JOIN (
    SELECT 'ir:*:report:%s:get' val, 'Get IR generation report with SourceKey = %s' descr FROM DUAL) new_perms
  WHERE sp.value LIKE 'source:%:access'
)
SELECT perms_for_all_sources.id,
       perms_for_all_sources.value,
       perms_for_all_sources.description,
       perms_for_all_sources.role_id
FROM perms_for_all_sources
MINUS
SELECT perms_for_all_sources.id,
       perms_for_all_sources.value,
       perms_for_all_sources.description,
       perms_for_all_sources.role_id
FROM perms_for_all_sources
       JOIN ${ohdsiSchema}.sec_permission ON perms_for_all_sources.value = ${ohdsiSchema}.sec_permission.value;

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
SELECT (${ohdsiSchema}.sec_role_permission_sequence.nextval), sp.role_id_tmp, sp.id
FROM ${ohdsiSchema}.sec_permission sp
WHERE sp.role_id_tmp IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.sec_permission
  DROP COLUMN role_id_tmp;