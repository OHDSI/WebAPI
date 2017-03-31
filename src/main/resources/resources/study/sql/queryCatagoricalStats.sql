-- (2/3) Get the categorical values from the cohort summary
-- and returned as part of the results.
select TOP 50
  ar1.covariate_id,
  ar1.covariate_name, 
  ar1.analysis_id,
  ar1.analysis_name, 
  ar1.domain_id,
  ar1.time_window,
  ar1.concept_id,
  sr1.count_value, 
  sr1.stat_value
from @results_database_schema.as_cohort_summary_analysis_ref ar1
inner join @results_database_schema.as_cohort_summary_results sr1 on ar1.covariate_id = sr1.covariate_id
left join @cdm_database_schema.CONCEPT c on ar1.concept_id = c.concept_id
where sr1.cohort_definition_id = @cohort_definition_id @criteria_clauses
ORDER BY sr1.stat_value DESC
