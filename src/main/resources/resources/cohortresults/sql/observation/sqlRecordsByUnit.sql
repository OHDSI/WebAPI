select c1.concept_id as observation_concept_id, 
	c1.concept_name as observation_concept_name,
	c2.concept_id as concept_id,
	c2.concept_name as concept_name, 
	hr1.count_value as count_value
from @resultsSchema.dbo.heracles_results hr1
	inner join @cdmSchema.dbo.concept c1 on hr1.stratum_1 = CAST(c1.concept_id as VARCHAR)
	inner join @cdmSchema.dbo.concept c2 on hr1.stratum_2 = CAST(c2.concept_id as VARCHAR)
where hr1.analysis_id = 807
and cohort_definition_id in (@cohortDefinitionId)