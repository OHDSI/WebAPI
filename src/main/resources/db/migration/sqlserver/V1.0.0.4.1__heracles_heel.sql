 IF OBJECT_ID('HERACLES_HEEL_results', 'U') IS NOT NULL 
 DROP TABLE HERACLES_HEEL_results;  
 
 CREATE TABLE HERACLES_HEEL_results 
 ( 
 cohort_definition_id int, 
 analysis_id INT, 
 HERACLES_HEEL_warning VARCHAR(255) 
 );