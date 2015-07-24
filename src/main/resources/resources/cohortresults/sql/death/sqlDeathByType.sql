select c2.concept_id as concept_id,
	c2.concept_name as concept_name, 
	hr1.count_value as count_value
from @ohdsi_database_schema.heracles_results hr1
	inner join  @cdm_database_schema.concept c2 on hr1.stratum_1 = CAST(c2.concept_id as VARCHAR(255))
where hr1.analysis_id = 505
and cohort_definition_id in (@cohortDefinitionId)