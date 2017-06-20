SELECT
	cca.analysis_id,
	s.source_id,
	s.source_key, 
	cca.target_cohort_id, 
	coalesce(td.short_name, td.cohort_definition_name) as target_cohort_name,
	cca.compare_cohort_id, 
	coalesce(cd.short_name, cd.cohort_definition_name) as compare_cohort_name,
	cca.outcome_cohort_id,
	coalesce(od.short_name, od.cohort_definition_name) as outcome_cohort_name,
	cca.t_at_risk_pp,
	cca.t_pt_pp,
	cca.t_cases_pp,
	cca.c_at_risk_pp,
	cca.c_pt_pp,
	cca.c_cases_pp,
	cca.relative_risk_pp,
	cca.lb_95_pp,
	cca.ub_95_pp,
	cca.pc_value_pp,
	cca.t_at_risk_itt,
	cca.t_pt_itt,
	cca.t_cases_itt,
	cca.c_at_risk_itt,
	cca.c_pt_itt,
	cca.c_cases_itt,
	cca.relative_risk_itt,
	cca.lb_95_itt,
	cca.ub_95_itt,
	cca.pc_value_itt
FROM @study_results_schema.cca_results cca
JOIN @study_results_schema.source s on s.source_id = cca.source_id
JOIN @study_results_schema.cohort_definition td on td.cohort_definition_id = cca.target_cohort_id
JOIN @study_results_schema.cohort_definition cd on cd.cohort_definition_id = cca.compare_cohort_id
JOIN @study_results_schema.cohort_definition od on od.cohort_definition_id = cca.outcome_cohort_id
WHERE (@pair_clauses) and cca.source_id in (@source_id_list)
