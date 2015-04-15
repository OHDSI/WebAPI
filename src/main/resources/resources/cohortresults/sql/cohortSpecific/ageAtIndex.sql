select cast(stratum_1 as integer) as age_at_index,
	count_value as num_persons
from @ohdsi_database_schema.heracles_results
where analysis_id in (1800)
and cohort_definition_id in (@cohortDefinitionId)