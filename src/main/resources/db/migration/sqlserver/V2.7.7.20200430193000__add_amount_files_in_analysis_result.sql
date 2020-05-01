IF COL_LENGTH('${ohdsiSchema}.analysis_execution', 'amount_files_in_analysis') IS NULL
  ALTER TABLE ${ohdsiSchema}.ee_analysis_status ADD amount_files_in_analysis BIGINT;
GO
