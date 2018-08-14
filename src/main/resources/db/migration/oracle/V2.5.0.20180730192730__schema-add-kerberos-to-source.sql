ALTER TABLE ${ohdsiSchema}.source
  ADD (krb_auth_method VARCHAR2(10) DEFAULT 'PASSWORD' NOT NULL);
ALTER TABLE ${ohdsiSchema}.source
  ADD (keytab_name VARCHAR2(50));
ALTER TABLE ${ohdsiSchema}.source
  ADD (krb_keytab BLOB);
ALTER TABLE ${ohdsiSchema}.source
  ADD (krb_admin_server VARCHAR2(50));