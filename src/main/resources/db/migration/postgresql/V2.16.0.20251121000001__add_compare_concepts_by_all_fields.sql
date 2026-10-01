-- Add new columns for vocab1 concept attributes
ALTER TABLE ${ohdsiSchema}.concept_set_compare_job_diff
ADD COLUMN vocab1_standard_concept VARCHAR(1),
ADD COLUMN vocab1_invalid_reason VARCHAR(1),
ADD COLUMN vocab1_concept_code VARCHAR(50),
ADD COLUMN vocab1_domain_id VARCHAR(20),
ADD COLUMN vocab1_vocabulary_id VARCHAR(20),
ADD COLUMN vocab1_concept_class_id VARCHAR(20),
ADD COLUMN vocab1_valid_start_date DATE,
ADD COLUMN vocab1_valid_end_date DATE;

-- Add new columns for vocab2 concept attributes
ALTER TABLE ${ohdsiSchema}.concept_set_compare_job_diff
ADD COLUMN vocab2_standard_concept VARCHAR(1),
ADD COLUMN vocab2_invalid_reason VARCHAR(1),
ADD COLUMN vocab2_concept_code VARCHAR(50),
ADD COLUMN vocab2_domain_id VARCHAR(20),
ADD COLUMN vocab2_vocabulary_id VARCHAR(20),
ADD COLUMN vocab2_concept_class_id VARCHAR(20),
ADD COLUMN vocab2_valid_start_date DATE,
ADD COLUMN vocab2_valid_end_date DATE;

-- Add mismatch flag columns
ALTER TABLE ${ohdsiSchema}.concept_set_compare_job_diff
ADD COLUMN standard_concept_mismatch BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN invalid_reason_mismatch BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN concept_code_mismatch BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN domain_id_mismatch BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN vocabulary_id_mismatch BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN concept_class_id_mismatch BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN valid_start_date_mismatch BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN valid_end_date_mismatch BOOLEAN NOT NULL DEFAULT FALSE;

-- Add indexes for performance on mismatch columns (works on all databases)
CREATE INDEX idx_cscdiff_standard_concept_mismatch 
ON ${ohdsiSchema}.concept_set_compare_job_diff(standard_concept_mismatch);

CREATE INDEX idx_cscdiff_invalid_reason_mismatch 
ON ${ohdsiSchema}.concept_set_compare_job_diff(invalid_reason_mismatch);

CREATE INDEX idx_cscdiff_concept_code_mismatch 
ON ${ohdsiSchema}.concept_set_compare_job_diff(concept_code_mismatch);

CREATE INDEX idx_cscdiff_domain_id_mismatch 
ON ${ohdsiSchema}.concept_set_compare_job_diff(domain_id_mismatch);

CREATE INDEX idx_cscdiff_vocabulary_id_mismatch 
ON ${ohdsiSchema}.concept_set_compare_job_diff(vocabulary_id_mismatch);

CREATE INDEX idx_cscdiff_concept_class_id_mismatch 
ON ${ohdsiSchema}.concept_set_compare_job_diff(concept_class_id_mismatch);

CREATE INDEX idx_cscdiff_valid_start_date_mismatch 
ON ${ohdsiSchema}.concept_set_compare_job_diff(valid_start_date_mismatch);

CREATE INDEX idx_cscdiff_valid_end_date_mismatch 
ON ${ohdsiSchema}.concept_set_compare_job_diff(valid_end_date_mismatch);