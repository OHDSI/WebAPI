select cast(hr1.stratum_1 as int) - MinValue.MinValue as interval_index, 
  hr1.count_value as count_value, 
	round(1.0*hr1.count_value / denom.count_value,5) as percent_value
from (select * from @ohdsi_database_schema.heracles_results where analysis_id = 3 and cohort_definition_id in (@cohortDefinitionId)) hr1,
	(select min(cast(stratum_1 as int)) as MinValue from @ohdsi_database_schema.heracles_results where analysis_id = 3 and cohort_definition_id in (@cohortDefinitionId)) MinValue,
	(select count_value from @ohdsi_database_schema.heracles_results where analysis_id = 1 and cohort_definition_id in (@cohortDefinitionId)) denom
order by hr1.stratum_1 asc
