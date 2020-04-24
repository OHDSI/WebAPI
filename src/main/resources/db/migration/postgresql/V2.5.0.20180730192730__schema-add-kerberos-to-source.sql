ALTER TABLE ${ohdsiSchema}.source ADD COLUMN krb_auth_method VARCHAR DEFAULT 'PASSWORD' NOT NULL;
ALTER TABLE ${ohdsiSchema}.source ADD COLUMN keytab_name VARCHAR;
ALTER TABLE ${ohdsiSchema}.source ADD COLUMN krb_keytab BYTEA;
ALTER TABLE ${ohdsiSchema}.source ADD COLUMN krb_admin_server VARCHAR;