select 'Length of observation' as series_name, 
	cast(ar1.stratum_1 as int)*30 as x_length_of_observation, 
	round(1.0*sum(ar2.count_value) / denom.count_value,5) as y_percent_persons
from (select * from @results_database_schema.ACHILLES_results where analysis_id = 108) ar1
inner join
(
	select * from @results_database_schema.ACHILLES_results where analysis_id = 108
) ar2 on ar1.analysis_id = ar2.analysis_id and cast(ar1.stratum_1 as int) <= cast(ar2.stratum_1 as int),
(
	select count_value from @results_database_schema.ACHILLES_results where analysis_id = 1
) denom
group by cast(ar1.stratum_1 as int)*30, denom.count_value
order by cast(ar1.stratum_1 as int)*30 asc
