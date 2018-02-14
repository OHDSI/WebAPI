IF OBJECT_ID('@results_schema.cohort', 'U') IS NULL
CREATE TABLE @results_schema.cohort
(
	COHORT_DEFINITION_ID int NOT NULL,
	SUBJECT_ID bigint NOT NULL,
	cohort_start_date date NOT NULL,
	cohort_end_date date NOT NULL
);
