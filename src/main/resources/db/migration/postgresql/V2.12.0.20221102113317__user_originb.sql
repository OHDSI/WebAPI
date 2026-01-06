ALTER TABLE ${ohdsiSchema}.sec_user ADD origin varchar(32) NULL;
UPDATE ${ohdsiSchema}.sec_user SET origin='SYSTEM';
ALTER TABLE ${ohdsiSchema}.sec_user ALTER COLUMN origin SET NOT NULL;
ALTER TABLE ${ohdsiSchema}.sec_user ALTER COLUMN origin SET DEFAULT 'SYSTEM';

ALTER TABLE ${ohdsiSchema}.sec_user_role ADD origin varchar(32) NULL;
UPDATE ${ohdsiSchema}.sec_user_role SET origin='SYSTEM';
ALTER TABLE ${ohdsiSchema}.sec_user_role ALTER COLUMN origin SET NOT NULL;
ALTER TABLE ${ohdsiSchema}.sec_user_role ALTER COLUMN origin SET DEFAULT 'SYSTEM';
