delete from ${ohdsiSchema}.sec_role_permission where
  permission_id in (select id from ${ohdsiSchema}.sec_permission where value in ('ir:*:delete'))
  AND role_id = (SELECT id FROM ${ohdsiSchema}.sec_role WHERE name = 'Atlas users');

delete from ${ohdsiSchema}.sec_role_permission where
  permission_id in (select id from ${ohdsiSchema}.sec_permission where value in ('ir:*:info'));

delete from ${ohdsiSchema}.sec_permission where
  value in ('ir:*:info');

insert into ${ohdsiSchema}.sec_permission(id, value, description)
    values
      (${ohdsiSchema}.sec_permission_id_seq.nextval, 'ir:*:info:get', 'Get IR info');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'ir:*:info:get'
  )
        AND sr.name IN ('Atlas users');


delete from ${ohdsiSchema}.sec_role_permission where
  permission_id in (select id from ${ohdsiSchema}.sec_permission where value like 'ir:%:report:*:get');
delete from ${ohdsiSchema}.sec_permission where value like 'ir:%:report:*:get';

alter table ${ohdsiSchema}.sec_permission add for_role_id int;

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, for_role_id)
  SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, REPLACE(CAST(new_perms.val AS VARCHAR(255)), '%s', REPLACE(REPLACE(value, 'source:', ''), ':access', '')), role_id
  FROM ${ohdsiSchema}.sec_permission sp
    JOIN ${ohdsiSchema}.sec_role_permission srp on sp.id = srp.permission_id
    CROSS JOIN (
                 SELECT 'ir:*:report:%s:get' val FROM dual
               ) new_perms
  WHERE sp.value LIKE 'source:%:access';

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
  SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sp.for_role_id, sp.id
  FROM ${ohdsiSchema}.sec_permission sp
  WHERE sp.for_role_id IS NOT NULL;

alter table ${ohdsiSchema}.sec_permission drop column for_role_id;