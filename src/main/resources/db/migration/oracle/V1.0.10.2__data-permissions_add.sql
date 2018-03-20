insert into ${ohdsiSchema}.sec_permission(id, value, description) values(112, 'vocabulary:*:get', 'Vocabulary services');
insert into ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id) values(sec_role_permission_sequence.NEXTVAL, 10, 112);
