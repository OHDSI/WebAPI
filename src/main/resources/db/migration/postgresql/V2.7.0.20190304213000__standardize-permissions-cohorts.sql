delete from ${ohdsiSchema}.sec_role_permission where
  permission_id in (select id from ${ohdsiSchema}.sec_permission where value in ('cohortdefinition:*:put', 'cohortdefinition:*:delete'))
  AND role_id = (SELECT id FROM ${ohdsiSchema}.sec_role WHERE name = 'Atlas users');

delete from ${ohdsiSchema}.sec_role_permission where
  permission_id in (select id from ${ohdsiSchema}.sec_permission where value in ('cohortdefinition:delete'));

-- Dummy permission: there is no such endpoint
delete from ${ohdsiSchema}.sec_permission where
  value in ('cohortdefinition:delete');