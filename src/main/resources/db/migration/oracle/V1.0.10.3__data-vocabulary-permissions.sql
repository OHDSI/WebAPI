insert into ${ohdsiSchema}.sec_permission(id, value, description) values(113, 'vocabulary:*:*:get', 'Vocabulary services');
insert into ${ohdsiSchema}.sec_permission(id, value, description) values(114, 'vocabulary:*:*:post', 'Vocabulary services');
insert into ${ohdsiSchema}.sec_permission(id, value, description) values(115, 'vocabulary:*:*:*:get', 'Vocabulary services');
insert into ${ohdsiSchema}.sec_permission(id, value, description) values(116, 'vocabulary:*:*:*:post', 'Vocabulary services');
insert into ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id) values(sec_role_permission_sequence.NEXTVAL, 10, 113);
insert into ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id) values(sec_role_permission_sequence.NEXTVAL, 10, 114);
insert into ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id) values(sec_role_permission_sequence.NEXTVAL, 10, 115);
insert into ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id) values(sec_role_permission_sequence.NEXTVAL, 10, 116);