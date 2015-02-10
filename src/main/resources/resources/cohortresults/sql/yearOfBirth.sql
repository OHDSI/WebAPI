select cast(stratum_1 as integer) as year_of_birth,
	count_value as num_persons
from @resultsSchema.dbo.@heraclesResultsTable
where analysis_id in (3)
and cohort_definition_id in (@cohortDefinitionId)