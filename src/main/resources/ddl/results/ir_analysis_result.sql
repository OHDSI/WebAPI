IF OBJECT_ID('@results_schema.ir_analysis_result', 'U') IS NULL
CREATE TABLE @results_schema.ir_analysis_result(
  analysis_id int NOT NULL,
  target_id int NOT NULL,
  outcome_id int NOT NULL,
  strata_mask bigint NOT NULL,
  person_count bigint NOT NULL,
  time_at_risk bigint NOT NULL,
  cases bigint NOT NULL
);
