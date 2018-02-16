ALTER TABLE ${ohdsiSchema}.sec_user DROP COLUMN IF EXISTS password; 
ALTER TABLE ${ohdsiSchema}.sec_user DROP COLUMN IF EXISTS salt; 