ALTER TABLE ${ohdsiSchema}.concept_set_compare_job
ADD COLUMN concept_sets_analyzed INTEGER,
ADD COLUMN concept_sets_with_diffs INTEGER;

CREATE INDEX idx_cs_compare_job_analyzed 
ON ${ohdsiSchema}.concept_set_compare_job(concept_sets_analyzed);

CREATE INDEX idx_cs_compare_job_with_diffs 
ON ${ohdsiSchema}.concept_set_compare_job(concept_sets_with_diffs);