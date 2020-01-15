IF OBJECT_ID('@results_schema.cohort_sample', 'U') IS NULL
CREATE TABLE @results_schema.cohort_sample_element(
    cohort_sample_id int NOT NULL,
    rank int NOT NULL,
    person_id bigint NOT NULL,
    age int,
    gender_concept_id int
);

CREATE INDEX idx_cohort_sample_element_rank ON @results_schema.cohort_sample_element (cohort_sample_id, rank);
