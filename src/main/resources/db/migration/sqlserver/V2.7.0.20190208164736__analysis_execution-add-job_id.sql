IF COL_LENGTH('${ohdsiSchema}.analysis_execution', 'job_execution_id') IS NULL
  ALTER TABLE ${ohdsiSchema}.analysis_execution ADD job_execution_id BIGINT;
GO
IF COL_LENGTH('${ohdsiSchema}.analysis_execution', 'analysis_type') IS NOT NULL
  ALTER TABLE ${ohdsiSchema}.analysis_execution DROP COLUMN analysis_type;
GO

IF COL_LENGTH('${ohdsiSchema}.output_files', 'media_type') IS NULL
  ALTER TABLE ${ohdsiSchema}.output_files ADD media_type VARCHAR(255);
GO

UPDATE ${ohdsiSchema}.analysis_execution SET sec_user_id = NULL
  WHERE NOT EXISTS(SELECT * FROM ${ohdsiSchema}.sec_user WHERE id = sec_user_id);

IF OBJECT_ID('${ohdsiSchema}.fk_ae_sec_user', 'F') IS NULL
  ALTER TABLE ${ohdsiSchema}.analysis_execution ADD CONSTRAINT fk_ae_sec_user FOREIGN KEY(sec_user_id)
    REFERENCES ${ohdsiSchema}.sec_user(id);
GO