CREATE TABLE ${ohdsiSchema}.ir_analysis_dist (
  analysis_id number(10) NOT NULL,
  target_id number(10) NOT NULL,
  outcome_id number(10) NOT NULL,
  strata_sequence number(10) NULL,
  dist_type number(10) NOT NULL,
  total number(19) NOT NULL,
  avg_value float NOT NULL,
  std_dev float NOT NULL,
  min_value number(10) NOT NULL,
  p10_value number(10) NOT NULL,
  p25_value number(10) NOT NULL,
  median_value number(10) NOT NULL,
  p75_value number(10) NOT NULL,
  p90_value number(10) NOT NULL,
  max_value number(10) NULL
)
;
