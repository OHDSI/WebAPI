	-- Distribution of age at cohort start by cohort start year

select c1.concept_name as Category,
hrd1.min_value as min_value,
hrd1.p10_value as p10_value,
hrd1.p25_value as p25_value,
hrd1.median_value as median_value,
hrd1.p75_value as p75_value,
hrd1.p90_value as p90_value,
hrd1.max_value as max_value,
0 as concept_id
from @resultsSchema.dbo.HERACLES_results_dist hrd1
inner join @cdmSchema.dbo.concept c1 on CAST(hrd1.stratum_1 AS INT) = c1.concept_id
where hrd1.analysis_id in (1803)
and cohort_definition_id in (@cohortDefinitionId)