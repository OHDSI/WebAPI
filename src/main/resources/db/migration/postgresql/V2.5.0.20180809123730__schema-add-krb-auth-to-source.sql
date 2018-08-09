ALTER TABLE ${ohdsiSchema}.source ADD COLUMN IF NOT EXISTS krb_auth_method VARCHAR DEFAULT 'PASSWORD' NOT NULL;
ALTER TABLE ${ohdsiSchema}.source ADD COLUMN IF NOT EXISTS krb_admin_server VARCHAR;