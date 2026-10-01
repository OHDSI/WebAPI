-- Add author and compareSourceCodes columns to concept_set_compare_job table
ALTER TABLE ${ohdsiSchema}.concept_set_compare_job
ADD COLUMN author VARCHAR(1024),
ADD COLUMN compare_source_codes BOOLEAN NOT NULL DEFAULT FALSE;

-- Add isSourceCode column to concept_set_compare_job_diff table
ALTER TABLE ${ohdsiSchema}.concept_set_compare_job_diff
ADD COLUMN is_source_code BOOLEAN NOT NULL DEFAULT FALSE;

-- Add index on author for filtering
CREATE INDEX idx_cs_compare_job_author 
ON ${ohdsiSchema}.concept_set_compare_job(author);

-- Add index on is_source_code for filtering
CREATE INDEX idx_cs_compare_job_diff_is_source_code 
ON ${ohdsiSchema}.concept_set_compare_job_diff(is_source_code);

-- Add composite index for common query patterns
CREATE INDEX idx_cs_compare_job_diff_job_source_code 
ON ${ohdsiSchema}.concept_set_compare_job_diff(compare_job_id, is_source_code);