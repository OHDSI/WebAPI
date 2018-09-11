IF OBJECT_ID('@results_schema.pathway_analysis_events', 'U') IS NULL
CREATE TABLE @results_schema.pathway_analysis_events
(
	pathway_analysis_generation_id INTEGER NOT NULL,
	combo_id INTEGER NOT NULL,
	subject_id INTEGER NOT NULL,
	cohort_start_date TIMESTAMP NOT NULL,
	cohort_end_date TIMESTAMP NOT NULL
);
