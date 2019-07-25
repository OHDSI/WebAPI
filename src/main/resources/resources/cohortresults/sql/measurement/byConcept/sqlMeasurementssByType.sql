select c1.concept_id as OBSERVATION_CONCEPT_ID, 
  c1.concept_name as OBSERVATION_CONCEPT_NAME, 
	c2.concept_id as CONCEPT_ID,
	c2.concept_name as CONCEPT_NAME, 
	hr1.count_value as COUNT_VALUE
from @ohdsi_database_schema.heracles_results hr1
	inner join @cdm_database_schema.concept c1
	  on CAST(CASE WHEN isNumeric(hr1.stratum_1) = 1 THEN hr1.stratum_1 ELSE null END AS INT) = c1.concept_id
	inner join @cdm_database_schema.concept c2
	  on CAST(CASE WHEN isNumeric(hr1.stratum_2) = 1 THEN hr1.stratum_2 ELSE null END AS INT) = c2.concept_id
where hr1.analysis_id = 1305
  and c1.concept_id = @conceptId
and cohort_definition_id = @cohortDefinitionId