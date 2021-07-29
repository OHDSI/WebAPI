IF OBJECT_ID('@results_schema.cohort_inclusion_stats_cache', 'U') IS NULL
CREATE TABLE @results_schema.cohort_inclusion_stats_cache (
  design_hash int NOT NULL,
  rule_sequence int NOT NULL,
  mode_id int NOT NULL,
  person_count bigint NOT NULL,
  gain_count bigint NOT NULL,
  person_total bigint NOT NULL
);
