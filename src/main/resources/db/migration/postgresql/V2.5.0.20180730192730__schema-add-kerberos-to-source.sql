ALTER TABLE ${ohdsiSchema}.source ADD COLUMN IF NOT EXISTS krb_keytab BYTEA;
ALTER TABLE ${ohdsiSchema}.source ADD COLUMN IF NOT EXISTS keytab_name VARCHAR;