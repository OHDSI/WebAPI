ALTER TABLE ${ohdsiSchema}.sec_role_group ADD job_id NUMBER(19);

ALTER TABLE ${ohdsiSchema}.sec_role_group
    ADD CONSTRAINT fk_role_group_job FOREIGN KEY(job_id)
    REFERENCES ${ohdsiSchema}.user_import_job(id) ON DELETE CASCADE;

ALTER TABLE ${ohdsiSchema}.sec_role_group DROP CONSTRAINT role_group_prov_uniq;

ALTER TABLE ${ohdsiSchema}.sec_role_group
    ADD CONSTRAINT UC_PROVIDER_GROUP_ROLE UNIQUE(provider, group_dn, role_id, job_id);