select S.cohort_definition_id
        , S.count_value as person_total
        , S.total/365.25 as exposure_years_total
        , (S.avg_value * 1000.0)/365.25 as exposure_avg_years_1k
from (
  select cohort_definition_id, analysis_id, count_value, avg_value, avg_value * count_value as total
  from @results_schema.heracles_results_dist
  where stratum_1 = ''
) S
where S.analysis_id = @analysis_id
  AND S.cohort_definition_id = @cohort_definition_id
