SELECT s.source_id,
	s.source_key, 
	os.target_cohort_id, 
	coalesce(td.short_name, td.cohort_definition_name) as target_cohort_name,
	os.outcome_cohort_id, 
	coalesce(od.short_name, od.cohort_definition_name) as outcome_cohort_name,
	os.at_risk_pp, os.cases_pp, os.pt_pp, os.ip_pp, os.ir_pp,
	os.cases_itt, os.pt_itt, os.ip_itt, os.ir_itt
FROM @study_results_schema.cohort_outcome_summary os
JOIN @study_results_schema.source s on os.source_id = s.source_id
JOIN @study_results_schema.cohort_definition td on td.cohort_definition_id = os.target_cohort_id
JOIN @study_results_schema.cohort_definition od on od.cohort_definition_id = os.outcome_cohort_id
WHERE (@pair_clauses)
	AND os.source_id IN (@source_id_list) 


