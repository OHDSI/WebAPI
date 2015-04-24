select table_name as SERIES_NAME,
	stratum_1 as X_CALENDAR_MONTH,
	count_value as Y_RECORD_COUNT
from
(
	select 'Visit occurrence' as table_name, stratum_1, count_value from @ohdsi_database_schema.heracles_results where analysis_id = 220 and cohort_definition_id in (@cohortDefinitionId)
	union all
	select 'Condition occurrence' as table_name, stratum_1, count_value from @ohdsi_database_schema.heracles_results where analysis_id = 420 and cohort_definition_id in (@cohortDefinitionId)
	union all
	select 'Death' as table_name, stratum_1, count_value from @ohdsi_database_schema.heracles_results where analysis_id = 502 and cohort_definition_id in (@cohortDefinitionId)
	union all
	select 'Procedure occurrence' as table_name, stratum_1, count_value from @ohdsi_database_schema.heracles_results where analysis_id = 620 and cohort_definition_id in (@cohortDefinitionId)
	union all
	select 'Drug exposure' as table_name, stratum_1, count_value from @ohdsi_database_schema.heracles_results where analysis_id = 720 and cohort_definition_id in (@cohortDefinitionId)
	union all
	select 'Observation' as table_name, stratum_1, count_value from @ohdsi_database_schema.heracles_results where analysis_id = 820 and cohort_definition_id in (@cohortDefinitionId)
	union all
	select 'Drug era' as table_name, stratum_1, count_value from @ohdsi_database_schema.heracles_results where analysis_id = 920 and cohort_definition_id in (@cohortDefinitionId)
	union all
	select 'Condition era' as table_name, stratum_1, count_value from @ohdsi_database_schema.heracles_results where analysis_id = 1020 and cohort_definition_id in (@cohortDefinitionId)
	union all
	select 'Observation period' as table_name, stratum_1, count_value from @ohdsi_database_schema.heracles_results where analysis_id = 111 and cohort_definition_id in (@cohortDefinitionId)
) t1
ORDER BY SERIES_NAME, CAST(stratum_1 as INT)