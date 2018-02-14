IF OBJECT_ID('@results_schema.HERACLES_HEEL_results', 'U') IS NULL
CREATE TABLE @results_schema.HERACLES_HEEL_results 
( 
  cohort_definition_id int, 
  analysis_id INT, 
  HERACLES_HEEL_warning VARCHAR(255) 
);
