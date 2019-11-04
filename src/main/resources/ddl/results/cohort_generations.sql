-- Historical generations table
IF OBJECT_ID('@results_schema.cohort_generations', 'U') IS NULL
CREATE TABLE @results_schema.cohort_generations
(
	generation_id int NOT NULL,
	SUBJECT_ID bigint NOT NULL,
	cohort_start_date date NOT NULL,
	cohort_end_date date NOT NULL
);

-- Link from all generations to the most recent generation for a cohort_definition_id
IF OBJECT_ID('@results_schema.cohort_generations_ref', 'U') IS NULL
CREATE TABLE @results_schema.cohort_generations_ref
(
	generation_id int NOT NULL,
	cohort_definition_id int NOT NULL
);