ALTER TABLE ${ohdsiSchema}.sec_user DROP CONSTRAINT sec_user_login_unique;

ALTER TABLE ${ohdsiSchema}.sec_user ALTER COLUMN login VARCHAR(1024);

ALTER TABLE ${ohdsiSchema}.sec_user ADD CONSTRAINT sec_user_login_unique UNIQUE (login);
