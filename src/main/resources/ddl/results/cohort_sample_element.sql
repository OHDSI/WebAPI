IF OBJECT_ID('@results_schema.cohort_sample', 'U') IS NULL
CREATE TABLE @results_schema.cohort_sample_element(
    id int NOT NULL,
    cohort_sample_id int NOT NULL,
    rank int NOT NULL,
    person_id bigint NOT NULL,
    age int NOT NULL,
    gender_concept_id int NOT NULL,
    CONSTRAINT fk_cohort_sample_element_id
        FOREIGN KEY (cohort_sample_id)
        REFERENCES @results_schema.cohort_sample (id)
        ON DELETE CASCADE,
);

CREATE CLUSTERED INDEX idx_cohort_sample_element_rank ON @results_schema.cohort_sample_element (cohort_sample_id, rank)

