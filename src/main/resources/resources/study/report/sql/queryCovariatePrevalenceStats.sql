with cteAllSources (source_id,cohort_definition_id, covariate_id) as (
	select s.source_id, c.cohort_definition_id, a.covariate_id
	from @study_results_schema.source s
	cross join @study_results_schema.cohort_summary_analysis_ref a
	cross join @study_results_schema.cohort_definition c
	where c.cohort_definition_id in (@cohort_id_list) and s.source_id in (@source_id_list) and a.covariate_id in (@covariate_id_list)
)
select a.source_id,
	s.source_key,
	s.source_name,
	a.cohort_definition_id,
  a.covariate_id,
	case when c.concept_id = 0 then ar1.covariate_name else c.concept_name end as covariate_name,
  ar1.covariate_name as long_name, 
	ar1.analysis_name,
	ar1.time_window,
  sr1.count_value, 
  sr1.stat_value
from cteAllSources a
join @study_results_schema.cohort_summary_analysis_ref ar1 on a.covariate_id = ar1.covariate_id
JOIN @study_results_schema.source s on s.source_id = a.source_id
left JOIN @study_results_schema.concept c on ar1.concept_id = c.concept_id
left join @study_results_schema.cohort_summary_results sr1 on ar1.covariate_id = sr1.covariate_id and sr1.cohort_definition_id = a.cohort_definition_id

