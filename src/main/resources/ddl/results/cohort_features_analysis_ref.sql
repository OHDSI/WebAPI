IF OBJECT_ID('@results_schema.cohort_features_analysis_ref', 'U') IS NULL
CREATE TABLE @results_schema.cohort_features_analysis_ref
(
  cohort_definition_id bigint,
  analysis_id int, 
  analysis_name varchar(1000), 
  domain_id varchar(100), 
  start_day int, 
  end_day int, 
  is_binary char,
  missing_means_zero char
);
