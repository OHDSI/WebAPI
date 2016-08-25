select t1.table_name as SERIES_NAME,
	t1.stratum_1 as X_CALENDAR_MONTH,
	round(1.0*t1.count_value/denom.count_value,5) as Y_RECORD_COUNT
from
(
	select 'Visit occurrence' as table_name, stratum_1, count_value from @results_database_schema.ACHILLES_results where analysis_id = 220
	union all
	select 'Condition occurrence' as table_name, stratum_1, count_value from @results_database_schema.ACHILLES_results where analysis_id = 420
	union all
	select 'Death' as table_name, stratum_1, count_value from @results_database_schema.ACHILLES_results where analysis_id = 502
	union all
	select 'Procedure occurrence' as table_name, stratum_1, count_value from @results_database_schema.ACHILLES_results where analysis_id = 620
	union all
	select 'Drug exposure' as table_name, stratum_1, count_value from @results_database_schema.ACHILLES_results where analysis_id = 720
	union all
	select 'Observation' as table_name, stratum_1, count_value from @results_database_schema.ACHILLES_results where analysis_id = 820
	union all
	select 'Drug era' as table_name, stratum_1, count_value from @results_database_schema.ACHILLES_results where analysis_id = 920
	union all
	select 'Condition era' as table_name, stratum_1, count_value from @results_database_schema.ACHILLES_results where analysis_id = 1020
	union all
	select 'Observation period' as table_name, stratum_1, count_value from @results_database_schema.ACHILLES_results where analysis_id = 111
) t1
inner join
(select * from @results_database_schema.ACHILLES_results where analysis_id = 117) denom
on t1.stratum_1 = denom.stratum_1
ORDER BY SERIES_NAME, CAST(t1.stratum_1 as INT)
