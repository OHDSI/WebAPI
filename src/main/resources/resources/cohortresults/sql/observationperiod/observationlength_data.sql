select cast(hr1.stratum_1 as int) as interval_index, 
	hr1.count_value as count_value, 
	round(1.0*hr1.count_value / denom.count_value,5) as percent_value
from @ohdsi_database_schema.heracles_results hr1,
(
	select count_value from @ohdsi_database_schema.heracles_results where analysis_id = 1 and cohort_definition_id = @cohortDefinitionId
) denom
where hr1.analysis_id = 108
and hr1.cohort_definition_id = @cohortDefinitionId
order by cast(hr1.stratum_1 as int) asc