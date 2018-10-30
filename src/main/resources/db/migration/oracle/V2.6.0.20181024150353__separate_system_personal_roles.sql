ALTER TABLE ${ohdsiSchema}.sec_role ADD system_role CHAR(1) DEFAULT '0' NOT NULL;

ALTER TABLE ${ohdsiSchema}.sec_role DROP CONSTRAINT SEC_ROLE_NAME_UQ;

UPDATE ${ohdsiSchema}.sec_role sr SET system_role = '1'
  WHERE NOT EXISTS(SELECT * FROM ${ohdsiSchema}.sec_user WHERE login = sr.name);

ALTER TABLE ${ohdsiSchema}.sec_role ADD CONSTRAINT SEC_ROLE_NAME_UQ UNIQUE (name, system_role);