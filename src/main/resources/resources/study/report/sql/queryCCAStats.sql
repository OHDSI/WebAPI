SELECT
	cca.analysis_id,
	s.source_key, 
	cca.target_cohort_id, 
	td.cohort_definition_name as target_cohort_name,
	cca.compare_cohort_id, 
	cd.cohort_definition_name as compare_cohort_name,
	cca.outcome_cohort_id,
	od.cohort_definition_name as outcome_cohort_name,
	cca.at_risk,
	cca.cases_pp,
	cca.pt_pp,
	cca.relative_risk_pp,
	cca.lb_95_pp,
	cca.ub_95_pp,
	cca.cases_itt,
	cca.pt_itt,
	cca.relative_risk_itt,
	cca.lb_95_itt,
	cca.ub_95_itt
FROM @study_results_schema.cca_results_sample cca
JOIN @study_results_schema.source s on s.source_id = cca.source_id
JOIN @study_results_schema.cohort_definition td on td.cohort_definition_id = cca.target_cohort_id
JOIN @study_results_schema.cohort_definition cd on cd.cohort_definition_id = cca.compare_cohort_id
JOIN @study_results_schema.cohort_definition od on od.cohort_definition_id = cca.outcome_cohort_id
WHERE (@pair_clauses) and cca.source_id in (@source_id_list)
