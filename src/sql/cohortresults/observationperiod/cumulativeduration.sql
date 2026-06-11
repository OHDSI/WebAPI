select 'Length of observation' as series_name, 
	x_length_of_observation,
	round(1.0*sum(ar2.count_value) / denom.count_value,5) as y_percent_persons
from (select *, cast(stratum_1 as int)*30 as x_length_of_observation from @ohdsi_database_schema.heracles_results where analysis_id = 108 and cohort_definition_id = @cohortDefinitionId) hr1
inner join
(
	select * from @ohdsi_database_schema.heracles_results where analysis_id = 108 and cohort_definition_id = @cohortDefinitionId
) ar2 on hr1.analysis_id = ar2.analysis_id and cast(hr1.stratum_1 as int) <= cast(ar2.stratum_1 as int),
(
	select count_value from @ohdsi_database_schema.heracles_results where analysis_id = 1 and cohort_definition_id = @cohortDefinitionId
) denom
group by x_length_of_observation, denom.count_value
order by x_length_of_observation asc
