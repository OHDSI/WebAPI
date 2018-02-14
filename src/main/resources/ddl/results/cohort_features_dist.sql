IF OBJECT_ID('@results_schema.cohort_features_dist', 'U') IS NULL
CREATE TABLE @results_schema.cohort_features_dist
(
  cohort_definition_id bigint,   
  covariate_id bigint, 
  count_value float, 
  min_value float, 
  max_value float, 
  average_value float, 
  standard_deviation float, 
  median_value float, 
  p10_value float, 
  p25_value float, 
  p75_value float, 
  p90_value float
);
