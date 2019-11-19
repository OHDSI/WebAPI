-- Add mode_id to cohort_inclusion_result table
IF NOT EXISTS (
  SELECT
    *
  FROM
    INFORMATION_SCHEMA.COLUMNS
  WHERE
    TABLE_SCHEMA = '${ohdsiSchema}' AND TABLE_NAME = 'cohort_inclusion_result' AND COLUMN_NAME = 'mode_id')
BEGIN
  ALTER TABLE [${ohdsiSchema}].[cohort_inclusion_result] ADD mode_id int NOT NULL DEFAULT 0
END;

-- Add mode_id to cohort_inclusion_stats table
IF NOT EXISTS (
  SELECT
    *
  FROM
    INFORMATION_SCHEMA.COLUMNS
  WHERE
    TABLE_SCHEMA = '${ohdsiSchema}' AND TABLE_NAME = 'cohort_inclusion_stats' AND COLUMN_NAME = 'mode_id')
BEGIN
  ALTER TABLE [${ohdsiSchema}].[cohort_inclusion_stats] ADD mode_id int NOT NULL DEFAULT 0
END;

-- Add mode_id to cohort_summary_stats table
IF NOT EXISTS (
  SELECT
    *
  FROM
    INFORMATION_SCHEMA.COLUMNS
  WHERE
    TABLE_SCHEMA = '${ohdsiSchema}' AND TABLE_NAME = 'cohort_summary_stats' AND COLUMN_NAME = 'mode_id')
BEGIN
  ALTER TABLE [${ohdsiSchema}].[cohort_summary_stats] ADD mode_id int NOT NULL DEFAULT 0
END;