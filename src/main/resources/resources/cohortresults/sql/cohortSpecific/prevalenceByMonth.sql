select hr1.stratum_1 as x_calendar_month,
	hr1.count_value as num_persons,
	round(1000*(1.0*hr1.count_value / t1.count_value),5) as y_prevalence_1000pp
from (select stratum_1, count_value 
	from @ohdsi_database_schema.heracles_results
	where analysis_id in (1815)
	and cohort_definition_id in (@cohortDefinitionId)
) hr1
	inner join 
(
	select stratum_1, count_value from @ohdsi_database_schema.heracles_results where analysis_id = 117 and cohort_definition_id in (@cohortDefinitionId)
) t1
on hr1.stratum_1 = t1.stratum_1