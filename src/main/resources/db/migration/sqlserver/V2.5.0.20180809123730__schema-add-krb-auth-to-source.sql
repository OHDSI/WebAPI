ALTER TABLE ${ohdsiSchema}.source
  ADD krb_auth_method VARCHAR(10) DEFAULT 'PASSWORD' NOT NULL;
ALTER TABLE ${ohdsiSchema}.source
  ADD krb_admin_server VARCHAR(50);