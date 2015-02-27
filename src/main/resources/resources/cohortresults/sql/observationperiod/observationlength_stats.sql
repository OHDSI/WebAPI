select  min(cast(hr1.stratum_1 as int)) * 30 as min_value, 
	max(cast(hr1.stratum_1 as int)) * 30 as max_value, 
	30 as interval_size
from @resultsSchema.dbo.heracles_analysis ha1
inner join @resultsSchema.dbo.heracles_results hr1 on ha1.analysis_id = hr1.analysis_id,
(
	select count_value from @resultsSchema.dbo.heracles_results where analysis_id = 1 and cohort_definition_id in (@cohortDefinitionId)
) denom
where ha1.analysis_id = 108
and hr1.cohort_definition_id in (@cohortDefinitionId)