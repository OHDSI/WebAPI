IF COL_LENGTH('${ohdsiSchema}.output_files', 'media_type') IS NULL
  ALTER TABLE ${ohdsiSchema}.output_files ADD media_type VARCHAR(255);
GO

ALTER TABLE ${ohdsiSchema}.output_files DROP CONSTRAINT fk_sif_cca_execution;
ALTER TABLE ${ohdsiSchema}.output_files DROP COLUMN cca_execution_id;

ALTER TABLE ${ohdsiSchema}.input_files DROP CONSTRAINT fk_sof_cca_execution;
ALTER TABLE ${ohdsiSchema}.input_files DROP COLUMN cca_execution_id;

IF COL_LENGTH('${ohdsiSchema}.output_files', 'execution_id') IS NULL
ALTER TABLE ${ohdsiSchema}.output_files ADD execution_id INTEGER;
GO
IF COL_LENGTH('${ohdsiSchema}.input_files', 'execution_id') IS NULL
ALTER TABLE ${ohdsiSchema}.input_files ADD execution_id INTEGER;
GO
IF OBJECT_ID('${ohdsiSchema}.output_file_seq', 'SO') IS NULL
CREATE SEQUENCE ${ohdsiSchema}.output_file_seq START WITH 1;
GO
IF OBJECT_ID('${ohdsiSchema}.input_file_seq', 'SO') IS NULL
CREATE SEQUENCE ${ohdsiSchema}.input_file_seq START WITH 1;
GO