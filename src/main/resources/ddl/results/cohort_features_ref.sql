IF OBJECT_ID('@results_schema.cohort_features_ref', 'U') IS NULL
CREATE TABLE @results_schema.cohort_features_ref
(
  cohort_definition_id bigint,
  covariate_id bigint, 
  covariate_name varchar(1000), 
  analysis_id int, 
  concept_id int
);
