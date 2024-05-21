select cohort_definition_id, analysis_id,
  cast(stratum_1 as varchar(255)), cast(stratum_2 as varchar(255)), cast(stratum_3 as varchar(255)), cast(stratum_4 as varchar(255)), cast(stratum_5 as varchar(255)),
  cast(count_value as bigint), cast(min_value as float), cast(max_value as float), cast(avg_value as float), cast(stdev_value as float),
  cast(median_value as float), cast(p10_value as float), cast(p25_value as float), cast(p75_value as float), cast(p90_value as float), GETDATE()
from #results_dist_@analysisId

