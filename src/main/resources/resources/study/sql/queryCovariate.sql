select 
  ar1.covariate_id,
  ar1.covariate_name, 
  ar1.analysis_id,
  ar1.analysis_name, 
  ar1.domain_id,
  ar1.time_window,
  ar1.concept_id,
  sr1.count_value, 
  sr1.stat_value,
  sr1.z_score,
  s1.source_id,
	s1.source_name,
  s1.source_key,
  cd1.cohort_definition_id,
  cd1.cohort_definition_name,
  cd1.short_name
from @study_results_schema.cohort_summary_analysis_ref ar1
join @study_results_schema.cohort_summary_results sr1 
	on ar1.covariate_id = sr1.covariate_id
inner join @study_results_schema.source s1
	on sr1.source_id = s1.source_id
inner join @study_results_schema.cohort_definition cd1
	on sr1.cohort_definition_id = cd1.cohort_definition_id
and cd1.cohort_definition_id @cohort_list_equality
where sr1.covariate_id = @covariate_id
;
