SELECT
	scca.analysis_id,
	s.source_key, 
	scca.target_cohort_id,
	td.cohort_definition_name as target_cohort_name,
	scca.outcome_cohort_id,
	od.cohort_definition_name as outcome_cohort_name,
	scca.relative_risk,
	scca.lb_95,
	scca.ub_95
FROM @study_results_schema.scca_results_sample scca
JOIN @study_results_schema.source s on s.source_id = scca.source_id
JOIN @study_results_schema.cohort_definition td on td.cohort_definition_id = scca.target_cohort_id
JOIN @study_results_schema.cohort_definition od on od.cohort_definition_id = scca.outcome_cohort_id
WHERE (@pair_clauses) and scca.source_id in (@source_id_list)
