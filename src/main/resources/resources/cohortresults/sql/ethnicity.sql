select c1.concept_id,
	c1.concept_name,
	hr1.count_value as num_persons
from
(
select cast(stratum_1 as integer) as concept_id,
	count_value
from @resultsSchema.dbo.@heraclesResultsTable
where analysis_id in (5)
and cohort_definition_id in (@cohortDefinitionId)
) hr1
inner join
@cdmSchema.dbo.concept c1
on hr1.concept_id = c1.concept_id
