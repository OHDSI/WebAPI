IF OBJECT_ID('@results_schema.ir_analysis_dist', 'U') IS NULL
CREATE TABLE @results_schema.ir_analysis_dist (
  analysis_id int NOT NULL,
  target_id int NOT NULL,
  outcome_id int NOT NULL,
  strata_sequence int NULL,
  dist_type int NOT NULL,
  total bigint NOT NULL,
  avg_value float NOT NULL,
  std_dev float NOT NULL,
  min_value int NOT NULL,
  p10_value int NOT NULL,
  p25_value int NOT NULL,
  median_value int NOT NULL,
  p75_value int NOT NULL,
  p90_value int NOT NULL,
  max_value int NULL
);

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

IF OBJECT_ID('@results_schema.ir_strata', 'U') IS NULL
CREATE TABLE @results_schema.ir_strata(
  analysis_id int NOT NULL,
  strata_sequence int NOT NULL,
  name varchar(255) NULL,
  description varchar(1000) NULL
);
