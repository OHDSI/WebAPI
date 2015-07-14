 IF OBJECT_ID('${ohdsiSchema}.HERACLES_HEEL_results', 'U') IS NOT NULL 
 DROP TABLE ${ohdsiSchema}.HERACLES_HEEL_results;  
 
 CREATE TABLE ${ohdsiSchema}.HERACLES_HEEL_results 
 ( 
 cohort_definition_id int, 
 analysis_id INT, 
 HERACLES_HEEL_warning VARCHAR(255) 
 );