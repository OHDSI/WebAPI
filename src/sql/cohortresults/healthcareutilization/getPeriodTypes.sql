select distinct p.period_type 
from @results_schema.heracles_results_dist h
  join @results_schema.heracles_periods p on (cast(p.period_id as varchar) = h.stratum_1)
where h.cohort_definition_id = @cohort_definition_id and h.analysis_id = @analysis_id