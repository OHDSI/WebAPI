CREATE SEQUENCE ${ohdsiSchema}.cohort_sample_sequence;

CREATE TABLE ${ohdsiSchema}.cohort_sample(
    id                   NUMBER(10) PRIMARY KEY,
    name                 VARCHAR(255) NOT NULL,
    cohort_definition_id NUMBER(10) NOT NULL,
    source_id            NUMBER(10) NOT NULL,
    "size"                 NUMBER(9) NOT NULL,
    age_min              NUMBER(4),
    age_max              NUMBER(4),
    age_mode             VARCHAR(24),
    gender_concept_ids   VARCHAR(255) NULL,
    created_by_id        INTEGER,
    created_date         TIMESTAMP WITH TIME ZONE DEFAULT sysdate NOT NULL,
    modified_by_id       INTEGER,
    modified_date        TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_cohort_sample_definition_id FOREIGN KEY (cohort_definition_id)
        REFERENCES ${ohdsiSchema}.cohort_definition (id) ON DELETE CASCADE,
    CONSTRAINT fk_cohort_sample_source_id FOREIGN KEY (source_id)
        REFERENCES ${ohdsiSchema}.source (source_id) ON DELETE CASCADE
);

CREATE INDEX idx_cohort_sample_source ON ${ohdsiSchema}.cohort_sample (cohort_definition_id, source_id);
