ALTER TABLE ${ohdsiSchema}.source
  ADD krb_auth_method VARCHAR(10) DEFAULT 'PASSWORD' NOT NULL, keytab_name VARCHAR(50), krb_keytab VARBINARY(MAX), krb_admin_server VARCHAR(50);