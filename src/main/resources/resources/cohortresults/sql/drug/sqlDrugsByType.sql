select c1.concept_id as drug_concept_id, 
	c2.concept_id as concept_id,
	c2.concept_name as concept_name, 
	ar1.count_value as count_value
from ACHILLES_results ar1
	inner join
	@cdmSchema.dbo.concept c1
	on ar1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
	inner join
	@cdmSchema.dbo.concept c2
	on ar1.stratum_2  = CAST(c2.concept_id AS VARCHAR)
where ar1.analysis_id = 705