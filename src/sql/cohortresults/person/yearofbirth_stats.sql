select min(cast(hr1.stratum_1 as int)) as min_value,
  max(cast(hr1.stratum_1 as int)) as max_value,
	1 as interval_size
from @ohdsi_database_schema.heracles_results hr1
where hr1.analysis_id = 3
and cohort_definition_id = @cohortDefinitionId
