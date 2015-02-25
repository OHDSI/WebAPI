select 	c1.concept_id,
	c1.concept_name as concept_path, 
	ar1.count_value as num_persons, 
	1.0*ar1.count_value / denom.count_value as percent_persons,
	1.0*ar2.count_value / ar1.count_value as records_per_person
from (select * from ACHILLES_results where analysis_id = 200) ar1
	inner join
	(select * from ACHILLES_results where analysis_id = 201) ar2 on ar1.stratum_1 = ar2.stratum_1
	inner join @cdmSchema.dbo.concept c1 on CAST(ar1.stratum_1 AS INT) = c1.concept_id,
	(select count_value from ACHILLES_results where analysis_id = 1) denom
order by ar1.count_value desc
