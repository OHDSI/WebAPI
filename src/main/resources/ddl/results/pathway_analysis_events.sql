IF OBJECT_ID('@results_schema.pathway_analysis_events', 'U') IS NULL
CREATE TABLE @results_schema.pathway_analysis_events
(
	pathway_analysis_generation_id BIGINT NOT NULL,
	combo_id BIGINT NOT NULL,
	subject_id BIGINT NOT NULL,
	cohort_start_date DATETIME NOT NULL,
	cohort_end_date DATETIME NOT NULL
);
