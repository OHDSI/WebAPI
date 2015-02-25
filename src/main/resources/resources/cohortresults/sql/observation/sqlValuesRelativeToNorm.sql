select c1.concept_id as observation_concept_id, 
	c1.concept_name as observation_concept_name,
	c2.concept_id as concept_id,
	c2.concept_name + ': ' + ar1.stratum_3 as concept_name, 
	ar1.count_value as count_value
from ACHILLES_results ar1
	inner join @cdmSchema.dbo.concept c1 on ar1.stratum_1 = CAST(c1.concept_id as VARCHAR)
	inner join @cdmSchema.dbo.concept c2 on ar1.stratum_2 = CAST(c2.concept_id as VARCHAR)
where ar1.analysis_id = 818