select cast(ar1.stratum_1 as int) as interval_index, 
	ar1.count_value as count_value, 
	round(1.0*ar1.count_value / denom.count_value,5) as percent_value
from @results_database_schema.ACHILLES_analysis aa1
inner join @results_database_schema.ACHILLES_results ar1 on aa1.analysis_id = ar1.analysis_id,
(
	select count_value from @results_database_schema.ACHILLES_results where analysis_id = 1
) denom
where aa1.analysis_id = 108
