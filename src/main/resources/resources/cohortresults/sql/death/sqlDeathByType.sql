select c2.concept_id as concept_id,
	c2.concept_name as concept_name, 
	hr1.count_value as count_value
from @resultsSchema.dbo.heracles_results hr1
	inner join  @cdmSchema.dbo.concept c2 on CAST(hr1.stratum_1 AS INT) = c2.concept_id
where hr1.analysis_id = 505
and cohort_definition_id in (@cohortDefinitionId)