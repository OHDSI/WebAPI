select distinct sp.value
from @ohdsi_schema.sec_user_role sur 
join @ohdsi_schema.sec_role_permission srp on sur.role_id = srp.role_id
join @ohdsi_schema.sec_permission sp on sp.id = srp.permission_id
where sur.user_id = ?
order by value;
