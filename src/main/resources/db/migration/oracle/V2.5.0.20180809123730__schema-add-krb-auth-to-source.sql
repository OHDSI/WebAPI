ALTER TABLE ${ohdsiSchema}.source
  ADD krb_auth_method VARCHAR2(10) DEFAULT 'PASSWORD' NOT NULL;
ALTER TABLE ${ohdsiSchema}.source
  ADD krb_admin_server VARCHAR2(50);