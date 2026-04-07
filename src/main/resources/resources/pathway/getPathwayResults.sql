SELECT pathway_analysis_generation_id, target_cohort_id, 
	step_1, step_2, step_3, step_4, step_5, step_6, step_7, step_8, step_9, step_10, count_value
FROM @target_database_schema.pathway_analysis_paths
WHERE pathway_analysis_generation_id = @generation_id;
