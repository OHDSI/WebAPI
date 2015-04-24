 select cast(cast(hrd1.stratum_1 as int)*10 as varchar) + '-' + cast((cast(hrd1.stratum_1 as int)+1)*10-1 as varchar)  as category,
  hrd1.min_value as min_value,
  hrd1.p10_value as p10_value,
  hrd1.p25_value as p25_value,
  hrd1.median_value as median_value,
  hrd1.p75_value as p75_value,
  hrd1.p90_value as p90_value,
  hrd1.max_value as max_value,
  0 as concept_id
from @ohdsi_database_schema.heracles_results_dist hrd1
where hrd1.analysis_id = 107
and cohort_definition_id in (@cohortDefinitionId)
order by cast(hrd1.stratum_1 as int) asc