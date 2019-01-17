select cast(ar1.stratum_1 as int) - MinValue.MinValue as interval_index, 
  ar1.count_value as count_value, 
  round(1.0*ar1.count_value / denom.count_value,5) as percent_value
from 
(
	select * from @results_database_schema.achilles_results where analysis_id = 109
) ar1,
(
	select min(cast(stratum_1 as int)) as MinValue 
	from @results_database_schema.achilles_results where analysis_id = 109
) MinValue,
(
	select count_value from @results_database_schema.achilles_results where analysis_id = 1
) denom