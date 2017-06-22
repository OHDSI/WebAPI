SELECT
	scca.analysis_id,
	s.source_id,
	s.source_key, 
	s.source_name,
	scca.target_cohort_id, 
	coalesce(td.short_name, td.cohort_definition_name) as target_cohort_name,
	scca.outcome_cohort_id,
	coalesce(od.short_name, od.cohort_definition_name) as outcome_cohort_name,
	scca.t_at_risk,
	scca.t_pt,
	scca.t_cases,
	scca.c_at_risk,
	scca.c_pt,
	scca.c_cases,
	scca.relative_risk,
	scca.lb_95,
	scca.ub_95,
	pc_value
FROM @study_results_schema.scca_results scca
JOIN @study_results_schema.source s on s.source_id = scca.source_id
JOIN @study_results_schema.cohort_definition td on td.cohort_definition_id = scca.target_cohort_id
JOIN @study_results_schema.cohort_definition od on od.cohort_definition_id = scca.outcome_cohort_id
WHERE (@pair_clauses) and scca.source_id in (@source_id_list)
