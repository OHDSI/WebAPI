insert into ${ohdsiSchema}.sec_permission(id, value, description) values
  (113, 'vocabulary:*:*:get', 'Vocabulary services'),
  (114, 'vocabulary:*:*:post', 'Vocabulary services'),
  (115, 'vocabulary:*:*:*:get', 'Vocabulary services'),
  (116, 'vocabulary:*:*:*:post', 'Vocabulary services');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values
  (10, 113),
  (10, 114),
  (10, 115),
  (10, 116);
