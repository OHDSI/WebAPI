ALTER TABLE ${ohdsiSchema}.sec_role ADD system_role BOOLEAN DEFAULT(FALSE) NOT NULL;

ALTER TABLE ${ohdsiSchema}.sec_role DROP CONSTRAINT sec_role_name_uq;

UPDATE ${ohdsiSchema}.sec_role SET system_role = TRUE
  WHERE NOT EXISTS(SELECT * FROM ${ohdsiSchema}.sec_user WHERE "login" = sec_role.name);

ALTER TABLE ${ohdsiSchema}.sec_role ADD CONSTRAINT sec_role_name_uq UNIQUE (name, system_role);