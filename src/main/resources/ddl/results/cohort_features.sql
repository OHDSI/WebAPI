IF OBJECT_ID('@results_schema.cohort_features', 'U') IS NULL
CREATE TABLE @results_schema.cohort_features
(
  cohort_definition_id bigint,
  covariate_id bigint, 
  sum_value bigint, 
  average_value float
);
