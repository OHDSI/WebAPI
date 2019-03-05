delete from ${ohdsiSchema}.sec_role_permission where
  permission_id in (select id from ${ohdsiSchema}.sec_permission where value in ('conceptset:*:delete', 'conceptset:*:put', 'conceptset:*:items:put'))
  AND role_id = (SELECT id FROM ${ohdsiSchema}.sec_role WHERE name = 'Atlas users');