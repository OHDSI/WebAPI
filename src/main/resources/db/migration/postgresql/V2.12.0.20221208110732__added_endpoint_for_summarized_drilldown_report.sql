create temp table temp_migration (
  from_perm_id int,
  new_value character varying(255)
);

insert into temp_migration (from_perm_id, new_value)
select sp.id                                                          as from_id,
       replace(cast(new_perms.val as VARCHAR(255)), '%s',
               replace(replace(value, 'source:', ''), ':access', '')) as new_value
from ${ohdsiSchema}.sec_permission sp
         cross join (
    select 'cdmresults:%s:multidrilldown:post' val
) new_perms
where sp.value like 'source:%:access';

insert into ${ohdsiSchema}.sec_permission (id, value)
select nextval('${ohdsiSchema}.sec_permission_id_seq'), new_value
from temp_migration;

insert into ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
select nextval('${ohdsiSchema}.sec_role_permission_sequence'), srp.role_id, sp.id as permission_id
from temp_migration m
         join ${ohdsiSchema}.sec_permission sp on
    m.new_value = sp.value
         join ${ohdsiSchema}.sec_role_permission srp on
    m.from_perm_id = srp.permission_id;

drop table temp_migration;