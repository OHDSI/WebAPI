select num.stratum_2 as x_calendar_month,
    round(1000*(1.0*num.count_value/denom.count_value),5) as y_prevalence_1000pp
from (
    select stratum_1, stratum_2, count_value 
    from @OHDSI_schema.@resultsSchema.dbo.heracles_results 
    where analysis_id = 402 and stratum_1 = '@conceptId' and cohort_definition_id in (@cohortDefinitionId)
) num
inner join (
    select stratum_1, count_value 
    from @OHDSI_schema.@resultsSchema.dbo.heracles_results 
    where analysis_id = 117 and cohort_definition_id in (@cohortDefinitionId)
) denom
on num.stratum_2 = denom.stratum_1 
order by cast(num.stratum_2 as int)