select s.source_id,
	s.source_key,
	sr1.cohort_definition_id,
  ar1.covariate_id,
	case when c.concept_id = 0 then ar1.covariate_name else c.concept_name end as covariate_name,
  ar1.covariate_name as long_name, 
	ar1.analysis_name,
	ar1.time_window,
  sr1.count_value, 
  sr1.stat_value
from @study_results_schema.cohort_summary_analysis_ref ar1
join @study_results_schema.cohort_summary_results sr1 on ar1.covariate_id = sr1.covariate_id
JOIN @study_results_schema.source s on s.source_id = sr1.source_id
JOIN @study_results_schema.concept c on ar1.concept_id = c.concept_id
where sr1.cohort_definition_id in (@cohort_id_list) and sr1.source_id in (@source_id_list) and ar1.covariate_id in (@covariate_id_list)
