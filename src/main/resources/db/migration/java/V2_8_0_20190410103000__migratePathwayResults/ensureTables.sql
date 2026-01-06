IF OBJECT_ID('@results_schema.pathway_analysis_codes', 'U') IS NULL
CREATE TABLE @results_schema.pathway_analysis_codes
(
	pathway_analysis_generation_id BIGINT NOT NULL,
	code BIGINT NOT NULL,
	name VARCHAR(2000) NOT NULL,
	is_combo int NOT NULL
);

IF OBJECT_ID('@results_schema.pathway_analysis_paths', 'U') IS NULL
CREATE TABLE @results_schema.pathway_analysis_paths
(
	pathway_analysis_generation_id BIGINT NOT NULL,
  target_cohort_id INTEGER NOT NULL,
  step_1 BIGINT,
  step_2 BIGINT,
  step_3 BIGINT,
  step_4 BIGINT,
  step_5 BIGINT,
  step_6 BIGINT,
  step_7 BIGINT,
  step_8 BIGINT,
  step_9 BIGINT,
  step_10 BIGINT,
  count_value BIGINT NOT NULL
);

-- verify tables exist in the correct structure
select pathway_analysis_generation_id, code, name, is_combo from @results_schema.pathway_analysis_codes WHERE 0 = 1;

select pathway_analysis_generation_id, target_cohort_id, step_1, step_2, step_3, step_4, step_5, step_6, step_7, step_8, step_9, step_10, count_value 
FROM @results_schema.pathway_analysis_paths where 0=1;
