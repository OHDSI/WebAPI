select c1.concept_id as concept_id,
  c1.concept_name as concept_name,
	ar1.count_value as count_value
from @results_database_schema.achilles_results ar1
	inner join
	@vocab_database_schema.concept c1
  on ar1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
where ar1.analysis_id = 4