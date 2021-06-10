select c1.concept_id as observation_concept_id, 
	c1.concept_name as observation_concept_name,
	c2.concept_id as concept_id,
	c2.concept_name as concept_name, 
	hr1.count_value as count_value
from @ohdsi_database_schema.heracles_results hr1
	inner join @cdm_database_schema.concept c1 on
	  on CAST(CASE WHEN isNumeric(hr1.stratum_1) = 1 THEN hr1.stratum_1 ELSE null END AS INT) = c1.concept_id
	inner join @cdm_database_schema.concept c2
	  on CAST(CASE WHEN isNumeric(hr1.stratum_2) = 1 THEN hr1.stratum_2 ELSE null END AS INT) = c2.concept_id
where hr1.analysis_id = 807
  and c1.concept_id = @conceptId
and cohort_definition_id = @cohortDefinitionId