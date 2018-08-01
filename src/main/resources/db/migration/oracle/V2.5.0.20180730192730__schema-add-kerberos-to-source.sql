ALTER TABLE ${ohdsiSchema}.source
  ADD krb_keytab VARCHAR2(3000);
ALTER TABLE ${ohdsiSchema}.source
  ADD keytab_name VARCHAR2(50);