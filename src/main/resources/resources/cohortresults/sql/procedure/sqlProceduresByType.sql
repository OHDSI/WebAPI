select c1.concept_id as procedure_concept_id, 
	c1.concept_name as procedure_concept_name,
	c2.concept_id as concept_id,
	c2.concept_name as concept_name, 
	ar1.count_value as count_value
from ACHILLES_results ar1
	inner join @cdmSchema.dbo.concept c1 on CAST(ar1.stratum_1 AS INT) = c1.concept_id
	inner join @cdmSchema.dbo.concept c2 on CAST(ar1.stratum_2 AS INT) = c2.concept_id
where ar1.analysis_id = 605