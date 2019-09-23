IF OBJECT_ID('@results_schema.cohort_censor_stats', 'U') IS NULL
CREATE TABLE @results_schema.cohort_censor_stats(
  generation_id int NOT NULL,
  lost_count BIGINT NOT NULL
);
