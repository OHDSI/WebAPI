IF OBJECT_ID('@results_schema.cohort_censor_stats_cache', 'U') IS NULL
CREATE TABLE @results_schema.cohort_censor_stats_cache (
  design_hash int NOT NULL,
  lost_count BIGINT NOT NULL
);
