ALTER TABLE ${ohdsiSchema}.analysis_execution ADD IF NOT EXISTS job_execution_id BIGINT;
ALTER TABLE ${ohdsiSchema}.analysis_execution DROP COLUMN IF EXISTS analysis_type;
ALTER TABLE ${ohdsiSchema}.output_files ADD IF NOT EXISTS media_type VARCHAR(255);

UPDATE ${ohdsiSchema}.analysis_execution SET sec_user_id = NULL
  WHERE NOT EXISTS(SELECT * FROM ${ohdsiSchema}.sec_user WHERE id = sec_user_id);

ALTER TABLE ${ohdsiSchema}.analysis_execution DROP CONSTRAINT IF EXISTS fk_ae_sec_user;
ALTER TABLE ${ohdsiSchema}.analysis_execution ADD CONSTRAINT fk_ae_sec_user FOREIGN KEY(sec_user_id)
  REFERENCES ${ohdsiSchema}.sec_user(id);