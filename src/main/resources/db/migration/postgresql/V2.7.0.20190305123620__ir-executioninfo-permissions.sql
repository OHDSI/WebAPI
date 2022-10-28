CREATE TEMP TABLE temp_migration (
  from_perm_id int,
  new_value character varying(255)
);

INSERT INTO temp_migration (from_perm_id, new_value)
SELECT sp.id as from_id,
  REPLACE(CAST(new_perms.val AS VARCHAR(255)), '%s', REPLACE(REPLACE(value, 'source:', ''), ':access', '')) as new_value
FROM ${ohdsiSchema}.sec_permission sp
CROSS JOIN (
  SELECT 'ir:*:info:%s:get' val
) new_perms
WHERE sp.value LIKE 'source:%:access';

INSERT INTO ${ohdsiSchema}.sec_permission (id, value)
SELECT nextval('${ohdsiSchema}.sec_permission_id_seq'), new_value
FROM temp_migration;

INSERT INTO ${ohdsiSchema}.sec_role_permission (id,role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'),
  srp.role_id,
  sp.id as permission_id
FROM temp_migration m
JOIN ${ohdsiSchema}.sec_permission sp on m.new_value = sp.value
JOIN ${ohdsiSchema}.sec_role_permission srp on m.from_perm_id = srp.permission_id;

drop table temp_migration;
