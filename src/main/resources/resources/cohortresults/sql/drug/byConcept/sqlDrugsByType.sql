select c1.concept_id as drug_concept_id, 
	c2.concept_id as concept_id,
	c2.concept_name as concept_name, 
	hr1.count_value as count_value
from @resultsSchema.dbo.heracles_results hr1
	inner join
	@cdmSchema.dbo.concept c1
	on hr1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
	inner join
	@cdmSchema.dbo.concept c2
	on hr1.stratum_2  = CAST(c2.concept_id AS VARCHAR)
where hr1.analysis_id = 705
and cohort_definition_id in (@cohortDefinitionId)
and c1.concept_id = @conceptId