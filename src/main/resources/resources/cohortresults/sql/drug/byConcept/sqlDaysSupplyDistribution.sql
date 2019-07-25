select c1.concept_id as concept_id,
	'Days supply' as category,
	hrd1.min_value as min_value,
	hrd1.p10_value as p10_value,
	hrd1.p25_value as p25_value,
	hrd1.median_value as median_value,
	hrd1.p75_value as p75_value,
	hrd1.p90_value as p90_value,
	hrd1.max_value as max_value
from @ohdsi_database_schema.heracles_results_dist hrd1
	inner join
	@cdm_database_schema.concept c1
	on CAST(CASE WHEN isNumeric(hrd1.stratum_1) = 1 THEN hrd1.stratum_1 ELSE null END AS INT) = c1.concept_id
where hrd1.analysis_id = 715
and hrd1.count_value > 0
and cohort_definition_id = @cohortDefinitionId
and c1.concept_id = @conceptId