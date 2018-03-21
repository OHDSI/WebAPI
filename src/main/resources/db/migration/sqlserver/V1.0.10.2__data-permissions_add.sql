insert into ${ohdsiSchema}.sec_permission(id, value, description) values(112, 'vocabulary:*:get', 'Vocabulary services');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(10, 112);
