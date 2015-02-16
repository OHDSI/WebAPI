select count_value,	min_value, max_value, avg_value, stdev_value, p10_value, p25_value, median_value, p75_value, p90_value
from @resultsSchema.dbo.heracles_results_dist
where analysis_id in (1801)
and cohort_definition_id in (@cohortDefinitionId)