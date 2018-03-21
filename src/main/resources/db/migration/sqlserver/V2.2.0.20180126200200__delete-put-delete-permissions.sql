DELETE FROM ${ohdsiSchema}.sec_role_permission WHERE permission_id in (
  SELECT id FROM ${ohdsiSchema}.sec_permission WHERE VALUE in (
    'role:1:permissions:*:put',
    'role:1:permissions:*:delete')
);

DELETE FROM ${ohdsiSchema}.sec_permission WHERE value in (
  'role:1:permissions:*:put',
  'role:1:permissions:*:delete');