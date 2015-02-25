select c1.concept_id as concept_id, 
  c1.concept_name as concept_name, 
	ar1.count_value as count_value
from ACHILLES_results ar1
	inner join
	@cdmSchema.dbo.concept c1
	on CAST(ar1.stratum_1 AS INT) = c1.concept_id
where ar1.analysis_id = 2
and c1.concept_id in (8507, 8532)