select cohort_definition_id, analysis_id, cast(stratum_1 as varchar(255)), cast(stratum_2 as varchar(255)),
  cast(stratum_3 as varchar(255)), cast(stratum_4 as varchar(255)), count_value
from #results_@analysisId
