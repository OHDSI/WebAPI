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
  sr1.z_score
from @study_results_schema.cohort_summary_analysis_ref ar1
join @study_results_schema.cohort_summary_results sr1 on ar1.covariate_id = sr1.covariate_id
where sr1.cohort_definition_id = @cohort_definition_id AND sr1.source_id = @source_id @criteria_clauses
ORDER BY sr1.stat_value DESC
