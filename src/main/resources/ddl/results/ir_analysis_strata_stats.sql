IF OBJECT_ID('@results_schema.ir_analysis_strata_stats', 'U') IS NULL
CREATE TABLE @results_schema.ir_analysis_strata_stats(
  analysis_id int NOT NULL,
  target_id int NOT NULL,
  outcome_id int NOT NULL,
  strata_sequence int NOT NULL,
  person_count bigint NOT NULL,
  time_at_risk bigint NOT NULL,
  cases bigint NOT NULL
);
