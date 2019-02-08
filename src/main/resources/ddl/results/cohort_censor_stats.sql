IF OBJECT_ID('@results_schema.cohort_censor_stats', 'U') IS NULL
CREATE TABLE @results_schema.cohort_censor_stats(
  cohort_definition_id int NOT NULL,
  lost_count BIGINT NOT NULL
);
