select c1.concept_id as concept_id,
	c2.concept_name as category,
	ard1.min_value as min_value,
	ard1.p10_value as p10_value,
	ard1.p25_value as p25_value,
	ard1.median_value as median_value,
	ard1.p75_value as p75_value,
	ard1.p90_value as p90_value,
	ard1.max_value as max_value
from @results_database_schema.ACHILLES_results_dist ard1
	inner join
	@vocab_database_schema.concept c1
	on ard1.stratum_1 = CAST(c1.concept_id as VARCHAR)
	inner join
	@vocab_database_schema.concept c2
	on ard1.stratum_2 = cast(c2.concept_id as VARCHAR)
where ard1.analysis_id = 906
and ard1.count_value > 0