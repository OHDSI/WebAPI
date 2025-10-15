-- Create sequence for concept_set_compare_job_stats
CREATE SEQUENCE ${ohdsiSchema}.concept_set_compare_job_stats_sequence START WITH 1 INCREMENT BY 1;

-- Create concept_set_compare_job_stats table
CREATE TABLE ${ohdsiSchema}.concept_set_compare_job_stats (
    id INTEGER NOT NULL,
    compare_job_id INTEGER NOT NULL,
    concept_set_id INTEGER NOT NULL,
    cs1_included_concepts_count INTEGER NOT NULL,
    cs1_included_source_codes_count INTEGER NOT NULL DEFAULT 0,
    cs2_included_concepts_count INTEGER NOT NULL,
    cs2_included_source_codes_count INTEGER NOT NULL DEFAULT 0,
    has_differences BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_cs_compare_job_stats PRIMARY KEY (id),
    CONSTRAINT fk_cs_compare_job_stats_job 
        FOREIGN KEY (compare_job_id) 
        REFERENCES ${ohdsiSchema}.concept_set_compare_job(id) 
        ON DELETE CASCADE
);

-- Add indexes for common query patterns
CREATE INDEX idx_cs_compare_job_stats_job_id 
ON ${ohdsiSchema}.concept_set_compare_job_stats(compare_job_id);

CREATE INDEX idx_cs_compare_job_stats_concept_set_id 
ON ${ohdsiSchema}.concept_set_compare_job_stats(concept_set_id);

CREATE INDEX idx_cs_compare_job_stats_has_diffs 
ON ${ohdsiSchema}.concept_set_compare_job_stats(has_differences);

CREATE INDEX idx_cs_compare_job_stats_job_concept_set 
ON ${ohdsiSchema}.concept_set_compare_job_stats(compare_job_id, concept_set_id);

CREATE INDEX idx_cs_compare_job_stats_job_diffs 
ON ${ohdsiSchema}.concept_set_compare_job_stats(compare_job_id, has_differences);

-- Add indexes for filtering by counts
CREATE INDEX idx_cs_compare_job_stats_cs1_concepts 
ON ${ohdsiSchema}.concept_set_compare_job_stats(cs1_included_concepts_count);

CREATE INDEX idx_cs_compare_job_stats_cs2_concepts 
ON ${ohdsiSchema}.concept_set_compare_job_stats(cs2_included_concepts_count);

CREATE INDEX idx_cs_compare_job_stats_cs1_source_codes 
ON ${ohdsiSchema}.concept_set_compare_job_stats(cs1_included_source_codes_count);

CREATE INDEX idx_cs_compare_job_stats_cs2_source_codes 
ON ${ohdsiSchema}.concept_set_compare_job_stats(cs2_included_source_codes_count);