SELECT s.source_key, os.target_cohort_id, os.outcome_cohort_id,
	os.at_risk_pp, os.cases_pp, os.pt_pp, os.ip_pp, os.ir_pp,
	os.cases_itt, os.pt_itt, os.ip_itt, os.ir_itt
FROM @study_results_schema.cohort_outcome_summary os
JOIN @study_results_schema.source s on os.source_id = s.source_id
WHERE os.target_cohort_id IN (@target_id_list) 
	AND os.outcome_cohort_id IN (@outcome_id_list)
	AND os.source_id IN (@source_id_list) 


