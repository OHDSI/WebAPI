-- Updates sec_permission_id_seq to maximum identity + 1
select setval('${ohdsiSchema}.sec_permission_id_seq', (select max(id) + 1 from ${ohdsiSchema}.sec_permission));