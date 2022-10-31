delete from ${ohdsiSchema}.sec_role_permission where
  permission_id in (select id from ${ohdsiSchema}.sec_permission where value in ('ir:*:delete'))
  AND role_id = (SELECT id FROM ${ohdsiSchema}.sec_role WHERE name = 'Atlas users');

delete from ${ohdsiSchema}.sec_role_permission where
  permission_id in (select id from ${ohdsiSchema}.sec_permission where value in ('ir:*:info'));

delete from ${ohdsiSchema}.sec_permission where
  value in ('ir:*:info');

insert into ${ohdsiSchema}.sec_permission(id, value, description)
    values
      (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'ir:*:info:get', 'Get IR info');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'ir:*:info:get'
  )
        AND sr.name IN ('Atlas users');


delete from ${ohdsiSchema}.sec_role_permission where
  permission_id in (select id from ${ohdsiSchema}.sec_permission where value like 'ir:%:report:*:get');
delete from ${ohdsiSchema}.sec_permission where value like 'ir:%:report:*:get';

CREATE TEMP TABLE temp_migration (
  from_perm_id int,
  new_value character varying(255)
);

INSERT INTO temp_migration (from_perm_id, new_value)
SELECT sp.id as from_id,
  REPLACE(CAST(new_perms.val AS VARCHAR(255)), '%s', REPLACE(REPLACE(value, 'source:', ''), ':access', '')) as new_value
FROM ${ohdsiSchema}.sec_permission sp
CROSS JOIN (
  SELECT 'ir:*:report:%s:get' val
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
