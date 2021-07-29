select CAST(CASE WHEN isNumeric(stratum_1) = 1 THEN stratum_1 ELSE null END AS INT) as age_at_index,
	count_value as num_persons
from @ohdsi_database_schema.heracles_results
where analysis_id in (1800)
and cohort_definition_id = @cohortDefinitionId