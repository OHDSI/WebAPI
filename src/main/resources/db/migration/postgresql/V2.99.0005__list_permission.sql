-- 'list' — list platform reference data (the user/role/permission registries, jobs, tools)
-- and use the DDL/SqlRender utilities. Granted to the built-in public role so authenticated
-- users keep listing access; anonymous is excluded.
--
-- Transitional 2.x -> 3.0 migration: the same permission and grant are also in the 3.0
-- baseline (B3.0.0) for fresh installs. No "where not exists" guard -- if the permission
-- already exists the migration should fail, since that is unexpected.
insert into ${ohdsiSchema}.sec_permission(id, value, description)
select nextval('${ohdsiSchema}.sec_permission_sequence'), value, description
from (
	values ('list', 'List platform reference data (users, roles, permissions, jobs, tools) and use DDL/SqlRender utilities')
) p (value, description);

insert into ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
select nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
from ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
where sp.value = 'list' and sr.name = 'public';
