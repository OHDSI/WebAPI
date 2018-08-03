ALTER TABLE ${ohdsiSchema}.source
  ADD krb_keytab VARBINARY(MAX), keytab_name VARCHAR(50);