select N.cohort_definition_id
  , N.stratum_1
  , P.period_type
  , P.period_start_date
  , P.period_end_date
  , N.count_value as person_total
  , ((N.count_value * 1.0)/D.count_value) * 100.0 as person_percent
  , N.total/365.25 as exposure_years_total
  , N.total/D.total * 100.0 as exposure_percent
  , (N.avg_value * 1000.0)/365.25 as exposure_avg_years_1k
from (
	select cohort_definition_id, analysis_id, stratum_1, count_value, avg_value, avg_value * count_value as total
	from @results_schema.heracles_results_dist where stratum_1 != ''
) N
inner join (
	select cohort_definition_id, analysis_id, stratum_1, count_value, avg_value, avg_value * count_value as total 
	from @results_schema.heracles_results_dist 
	where stratum_1 = ''
) D on N.analysis_id = D.analysis_id
	and N.cohort_definition_id = D.cohort_definition_id
left join @results_schema.heracles_periods P on N.stratum_1 = cast(P.period_id as varchar(19))
where N.analysis_id = @analysis_id
	AND N.cohort_definition_id = @cohort_definition_id
  AND P.period_type = '@period_type'
ORDER BY P.period_start_date
