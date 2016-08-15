select  min(cast(ar1.stratum_1 as int)) * 30 as min_value, 
	max(cast(ar1.stratum_1 as int)) * 30 as max_value, 
	30 as interval_size
from @results_database_schema.ACHILLES_analysis aa1
inner join @results_database_schema.ACHILLES_results ar1 on aa1.analysis_id = ar1.analysis_id,
(
	select count_value from @results_database_schema.ACHILLES_results where analysis_id = 1
) denom
where aa1.analysis_id = 108