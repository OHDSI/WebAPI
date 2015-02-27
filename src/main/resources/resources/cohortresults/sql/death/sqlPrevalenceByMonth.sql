select num.stratum_1 as x_calendar_month,   -- calendar year, note, there could be blanks
	1000*(1.0*num.count_value/denom.count_value) as y_prevalence_1000pp  --prevalence, per 1000 persons
from 
	(select * from @resultsSchema.dbo.heracles_results where analysis_id = 502 and cohort_definition_id in (@cohortDefinitionId)) num
	inner join
	(select * from @resultsSchema.dbo.heracles_results where analysis_id = 117 and cohort_definition_id in (@cohortDefinitionId)) denom on num.stratum_1 = denom.stratum_1  --calendar year
ORDER BY CAST(num.stratum_1 as INT)
