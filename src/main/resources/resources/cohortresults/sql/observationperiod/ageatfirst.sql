select cast(hr1.stratum_1 as int) as interval_index, 
	hr1.count_value as count_value, 
	round(1.0*hr1.count_value / denom.count_value,5) as percent_value
from 
(
	select * from @ohdsi_database_schema.heracles_results where analysis_id = 101 and cohort_definition_id = @cohortDefinitionId
) hr1,
(
	select count_value from @ohdsi_database_schema.heracles_results where analysis_id = 1 and cohort_definition_id = @cohortDefinitionId
) denom
order by cast(hr1.stratum_1 as int) asc