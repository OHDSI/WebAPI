select s.source_id,
	s.source_key,
	s.source_name,
	sr1.cohort_definition_id,
	cd.cohort_definition_name,
  ar1.covariate_id,
  ar1.covariate_name, 
	ar1.analysis_name,
	ar1.time_window,
	sr1.count_value, 
	sr1.min_value, 
	sr1.max_value, 
	sr1.avg_value, 
	sr1.stdev_value, 
	sr1.median_value, 
	sr1.p10_value, 
	sr1.p25_value, 
	sr1.p75_value, 
	sr1.p90_value
from @study_results_schema.cohort_summary_analysis_ref ar1
join @study_results_schema.cohort_summary_results_dist sr1 on ar1.covariate_id = sr1.covariate_id
JOIN @study_results_schema.source s on s.source_id = sr1.source_id
JOIN @study_results_schema.cohort_definition cd on cd.cohort_definition_id = sr1.cohort_definition_id
where sr1.cohort_definition_id in (@cohort_id_list) and sr1.source_id in (@source_id_list) and ar1.covariate_id in (@covariate_id_list)
