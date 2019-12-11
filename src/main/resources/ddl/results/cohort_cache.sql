IF OBJECT_ID('@results_schema.cohort_cache', 'U') IS NULL
CREATE TABLE @results_schema.cohort_cache (
	design_hash int NOT NULL,
	SUBJECT_ID bigint NOT NULL,
	cohort_start_date date NOT NULL,
	cohort_end_date date NOT NULL
);
