-- Add concept_set_ids column to store the filter
ALTER TABLE ${ohdsiSchema}.concept_set_compare_job
ADD COLUMN concept_set_ids VARCHAR(5000);

-- Add index for potential filtering
CREATE INDEX idx_cs_compare_job_concept_set_ids 
ON ${ohdsiSchema}.concept_set_compare_job(concept_set_ids);