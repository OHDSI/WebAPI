SELECT
	scca.analysis_id,
	s.source_key, 
	scca.target_cohort_id, 
	td.cohort_definition_name as target_cohort_name,
	scca.outcome_cohort_id,
	od.cohort_definition_name as outcome_cohort_name,
	scca.at_risk,
	scca.cases_pp,
	scca.pt_pp,
	scca.relative_risk_pp,
	scca.lb_95_pp,
	scca.ub_95_pp,
	scca.cases_itt,
	scca.pt_itt,
	scca.relative_risk_itt,
	scca.lb_95_itt,
	scca.ub_95_itt
FROM @study_results_schema.scca_results_sample scca
JOIN @study_results_schema.source s on s.source_id = scca.source_id
JOIN @study_results_schema.cohort_definition td on td.cohort_definition_id = scca.target_cohort_id
JOIN @study_results_schema.cohort_definition od on od.cohort_definition_id = scca.outcome_cohort_id
WHERE (@pair_clauses) and scca.source_id in (@source_id_list)
