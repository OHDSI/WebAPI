IF OBJECT_ID('@results_schema.cohort_summary_stats', 'U') IS NULL
CREATE TABLE @results_schema.cohort_summary_stats(
  cohort_definition_id int NOT NULL,
  mode_id int NOT NULL,
  base_count bigint NOT NULL,
  final_count bigint NOT NULL
);
