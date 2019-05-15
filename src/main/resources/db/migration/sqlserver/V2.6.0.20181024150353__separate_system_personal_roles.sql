ALTER TABLE ${ohdsiSchema}.sec_role ADD system_role BIT CONSTRAINT DF_ROLE_SYSTEM DEFAULT(0) NOT NULL
Go

ALTER TABLE ${ohdsiSchema}.sec_role DROP CONSTRAINT sec_role_name_uq
Go

UPDATE ${ohdsiSchema}.sec_role SET system_role = 1
  WHERE NOT EXISTS(SELECT * FROM ${ohdsiSchema}.sec_user WHERE [login] = sec_role.name)
Go

ALTER TABLE ${ohdsiSchema}.sec_role ADD CONSTRAINT sec_role_name_uq UNIQUE (name, system_role);