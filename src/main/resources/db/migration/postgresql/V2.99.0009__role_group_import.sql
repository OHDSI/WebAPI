-- Rename sec_role_group table and related objects to sec_group_role_import
-- to clarify that this table is specifically for LDAP/AD import-based group-to-role mapping

ALTER SEQUENCE ${ohdsiSchema}.sec_role_group_seq RENAME TO sec_group_role_import_seq;

ALTER TABLE ${ohdsiSchema}.sec_role_group RENAME TO sec_group_role_import;

ALTER TABLE ${ohdsiSchema}.sec_group_role_import RENAME CONSTRAINT sec_role_group_pkey TO pk_sec_group_role_import;

ALTER TABLE ${ohdsiSchema}.sec_group_role_import RENAME CONSTRAINT fk_role_group_job TO fk_group_role_import_job;
