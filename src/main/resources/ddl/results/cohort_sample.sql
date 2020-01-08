IF OBJECT_ID('@results_schema.cohort_sample', 'U') IS NULL
CREATE TABLE @results_schema.cohort_sample(
    id int NOT NULL,
    name varchar(255) NOT NULL,
    cohort_definition_id int NOT NULL,
    age_min int NOT NULL,
    age_max int NOT NULL,
    gender_concept_id int NOT NULL,
    size int NOT NULL,
    CONSTRAINT pk_cohort_sample_id PRIMARY KEY (id)
);
