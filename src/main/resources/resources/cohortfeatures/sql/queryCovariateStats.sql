select
  f.covariate_id,
  fr.covariate_name, 
  ar.analysis_id,
  ar.analysis_name, 
  ar.domain_id,
  ar.start_day,
  ar.end_day,
  fr.concept_id,
	c.concept_name,
  f.sum_value as count_value, 
  f.average_value as stat_value
from @cdm_results_schema.cohort_features f
join @cdm_results_schema.cohort_features_ref fr on fr.covariate_id = f.covariate_id and fr.cohort_definition_id = f.cohort_definition_id
JOIN @cdm_results_schema.cohort_features_analysis_ref ar on ar.analysis_id = fr.analysis_id and ar.cohort_definition_id = fr.cohort_definition_id
LEFT JOIN @cdm_database_schema.concept c on c.concept_id = fr.concept_id
where f.cohort_definition_id = @cohort_definition_id AND f.average_value > .005 @criteria_clauses
ORDER BY f.average_value DESC
