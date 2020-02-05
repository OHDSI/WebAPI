IF OBJECT_ID('@results_schema.cohort_inclusion_result_cache', 'U') IS NULL
CREATE TABLE @results_schema.cohort_inclusion_result_cache (
  design_hash int NOT NULL,
  mode_id int NOT NULL,
  inclusion_rule_mask bigint NOT NULL,
  person_count bigint NOT NULL
);
