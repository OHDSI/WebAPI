select c1.concept_id as concept_id,
	c2.concept_name as category,
	hrd1.min_value as min_value,
	hrd1.p10_value as P10_value,
	hrd1.p25_value as P25_value,
	hrd1.median_value as median_value,
	hrd1.p75_value as P75_value,
	hrd1.p90_value as P90_value,
	hrd1.max_value as max_value
from @ohdsi_database_schema.heracles_results_dist hrd1
	inner join @cdm_database_schema.concept c1
	  on CAST(CASE WHEN isNumeric(hrd1.stratum_1) = 1 THEN hrd1.stratum_1 ELSE null END AS INT) = c1.concept_id
	inner join @cdm_database_schema.concept c2
	  on CAST(CASE WHEN isNumeric(hrd1.stratum_2) = 1 THEN hrd1.stratum_2 ELSE null END AS INT) = c2.concept_id
where hrd1.analysis_id = 1315
and hrd1.count_value > 0
  and c1.concept_id = @conceptId
and cohort_definition_id = @cohortDefinitionId

