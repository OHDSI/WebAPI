select count_value as num_persons
from @resultsSchema.dbo.heracles_results
where analysis_id in (1)
and cohort_definition_id in (@cohortDefinitionId)