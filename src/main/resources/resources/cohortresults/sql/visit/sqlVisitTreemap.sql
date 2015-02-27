select 	c1.concept_id,
	c1.concept_name as concept_path, 
	hr1.count_value as num_persons, 
	1.0*hr1.count_value / denom.count_value as percent_persons,
	1.0*hr2.count_value / hr1.count_value as records_per_person
from (select * from @resultsSchema.dbo.heracles_results where analysis_id = 200 and cohort_definition_id in (@cohortDefinitionId)) hr1
	inner join
	(select * from @resultsSchema.dbo.heracles_results where analysis_id = 201 and cohort_definition_id in (@cohortDefinitionId)) hr2 on hr1.stratum_1 = hr2.stratum_1
	inner join @cdmSchema.dbo.concept c1 on CAST(hr1.stratum_1 AS INT) = c1.concept_id,
	(select count_value from @resultsSchema.dbo.heracles_results where analysis_id = 1 and cohort_definition_id in (@cohortDefinitionId)) denom
order by hr1.count_value desc
