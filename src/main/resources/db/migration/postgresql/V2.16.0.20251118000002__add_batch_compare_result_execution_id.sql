ALTER TABLE ${ohdsiSchema}.concept_set_compare_job 
ADD COLUMN execution_id BIGINT;


ALTER TABLE ${ohdsiSchema}.concept_set_compare_job 
ADD CONSTRAINT uk_compare_job_execution_id UNIQUE (execution_id);


