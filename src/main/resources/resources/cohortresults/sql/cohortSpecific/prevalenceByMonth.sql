select hr1.stratum_1 as calendar_month,
	substring(hr1.stratum_1,1,4) as calendar_year,
	substring(hr1.stratum_1,5,2) as calendar_month_index,
	hr1.count_value as num_persons,
	round(1000*(1.0*hr1.count_value / t1.count_value),5) as y_prevelance_1000pp
from (select stratum_1, count_value 
	from @resultsSchema.dbo.heracles_results
	where analysis_id in (1815)
	and cohort_definition_id in (@cohortDefinitionId)
) hr1
	inner join 
(
	select stratum_1, count_value from @resultsSchema.dbo.@resultsSchema.dbo.heracles_results where analysis_id = 117 and cohort_definition_id in (@cohortDefinitionId)
) t1
on hr1.stratum_1 = t1.stratum_1