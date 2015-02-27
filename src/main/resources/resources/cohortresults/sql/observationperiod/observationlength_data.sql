select cast(hr1.stratum_1 as int) as interval_index, 
	hr1.count_value as count_value, 
	round(1.0*hr1.count_value / denom.count_value,5) as percent_value
from @resultsSchema.dbo.heracles_analysis ha1
inner join @resultsSchema.dbo.heracles_results hr1 on ha1.analysis_id = hr1.analysis_id,
(
	select count_value from @resultsSchema.dbo.heracles_results where analysis_id = 1 and cohort_definition_id in (@cohortDefinitionId)
) denom
where ha1.analysis_id = 108
and hr1.cohort_definition_id in (@cohortDefinitionId)
order by cast(hr1.stratum_1 as int) asc