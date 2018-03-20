insert into ${ohdsiSchema}.sec_permission(id, value, description) values(112, 'vocabulary:*:get', 'Vocabulary services') on conflict do nothing;
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(10, 112) on conflict do nothing;
