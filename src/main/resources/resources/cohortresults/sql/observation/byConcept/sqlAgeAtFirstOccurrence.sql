select c1.concept_id as CONCEPT_ID,
	c2.concept_name as CATEGORY,
	hrd1.min_value as MIN_VALUE,
	hrd1.p10_value as P10_VALUE,
	hrd1.p25_value as P25_VALUE,
	hrd1.median_value as MEDIAN_VALUE,
	hrd1.p75_value as P75_VALUE,
	hrd1.p90_value as P90_VALUE,
	hrd1.max_value as MAX_VALUE
from @ohdsi_database_schema.heracles_results_dist hrd1
	inner join @cdm_database_schema.concept c1 on hrd1.stratum_1 = CAST(c1.concept_id as VARCHAR)
	inner join @cdm_database_schema.concept c2 on hrd1.stratum_2 = CAST(c2.concept_id as VARCHAR)
where hrd1.analysis_id = 806
  and c1.concept_id = @conceptId
and cohort_definition_id in (@cohortDefinitionId)
