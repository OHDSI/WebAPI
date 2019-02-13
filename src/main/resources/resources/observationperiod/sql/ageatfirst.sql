select cast(ar1.stratum_1 as int) as interval_index, 
	ar1.count_value as count_value, 
	round(1.0*ar1.count_value / denom.count_value,5) as percent_value
from 
(
	select * from @results_database_schema.achilles_results where analysis_id = 101
) ar1,
(
	select count_value from @results_database_schema.achilles_results where analysis_id = 1
) denom