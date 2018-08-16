select  min(cast(hr1.stratum_1 as int)) * 30 as min_value, 
	max(cast(hr1.stratum_1 as int)) * 30 as max_value, 
	30 as interval_size
from @ohdsi_database_schema.heracles_results hr1,
(
	select count_value from @ohdsi_database_schema.heracles_results where analysis_id = 1 and cohort_definition_id = @cohortDefinitionId
) denom
where hr1.analysis_id = 108
and hr1.cohort_definition_id = @cohortDefinitionId