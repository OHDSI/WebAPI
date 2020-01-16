CREATE SEQUENCE ${ohdsiSchema}.cohort_sample_sequence;

CREATE TABLE ${ohdsiSchema}.cohort_sample(
    id                   NUMBER(19) PRIMARY KEY CLUSTERED NOT NULL,
    name                 VARCHAR(255) NOT NULL,
    cohort_definition_id INTEGER NOT NULL,
    source_id            INTEGER NOT NULL,
    size                 NUMBER(9) NOT NULL,
    age_min              NUMBER(4) NULL,
    age_max              NUMBER(4) NULL,
    age_mode             VARCHAR(24),
    gender_concept_ids   VARCHAR(255) NULL,
    created_by_id        INTEGER,
    created_date         DATETIMEOFFSET,
    modified_by_id       INTEGER,
    modified_date        DATETIMEOFFSET,
    CONSTRAINT fk_cohort_sample_definition_id FOREIGN KEY (cohort_definition_id)
        REFERENCES ${ohdsiSchema}.cohort_definition (id) ON DELETE CASCADE,
    CONSTRAINT fk_cohort_sample_source_id FOREIGN KEY (source_id)
        REFERENCES ${ohdsiSchema}.source (source_id) ON DELETE CASCADE
);

CREATE INDEX idx_cohort_sample_source ON ${ohdsiSchema}.cohort_sample (cohort_definition_id, source_id);
