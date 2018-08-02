ALTER TABLE ${ohdsiSchema}.source
  ADD krb_keytab BLOB;
ALTER TABLE ${ohdsiSchema}.source
  ADD keytab_name VARCHAR2(50);