select row_number() over (order by hr1.stratum_1) as concept_id, 
	hr1.stratum_1 as concept_name, 
	hr1.count_value as count_value
from @resultsSchema.dbo.heracles_results hr1
where hr1.analysis_id = 113
and cohort_definition_id in (@cohortDefinitionId)