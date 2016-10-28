select c1.concept_id as CONCEPT_ID,
	c2.concept_name as CATEGORY,
	ard1.min_value as MIN_VALUE,
	ard1.p10_value as P10_VALUE,
	ard1.p25_value as P25_VALUE,
	ard1.median_value as MEDIAN_VALUE,
	ard1.p75_value as P75_VALUE,
	ard1.p90_value as P90_VALUE,
	ard1.max_value as MAX_VALUE
from @results_database_schema.ACHILLES_results_dist ard1
	inner join @vocab_database_schema.concept c1 on ard1.stratum_1 = CAST(c1.concept_id as VARCHAR)
	inner join @vocab_database_schema.concept c2 on ard1.stratum_2 = CAST(c2.concept_id as VARCHAR)
where ard1.analysis_id = 806
