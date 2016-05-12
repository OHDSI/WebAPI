select c1.concept_id as OBSERVATION_CONCEPT_ID, 
  c1.concept_name as OBSERVATION_CONCEPT_NAME, 
	c2.concept_id as CONCEPT_ID,
	c2.concept_name as CONCEPT_NAME, 
	ar1.count_value as COUNT_VALUE
from @results_database_schema.ACHILLES_results ar1
	inner join @vocab_database_schema.concept c1 on ar1.stratum_1 = CAST(c1.concept_id as VARCHAR)
	inner join @vocab_database_schema.concept c2 on ar1.stratum_2 = CAST(c2.concept_id as VARCHAR)
where ar1.analysis_id = 805