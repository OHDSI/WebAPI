 select CONCAT(cast(cast(ard1.stratum_1 as int)*10 as varchar(11)), '-', cast((cast(ard1.stratum_1 as int)+1)*10-1 as varchar(11)))  as category,
  ard1.min_value as min_value,
  ard1.p10_value as p10_value,
  ard1.p25_value as p25_value,
  ard1.median_value as median_value,
  ard1.p75_value as p75_value,
  ard1.p90_value as p90_value,
  ard1.max_value as max_value
from @results_database_schema.achilles_results_dist ard1
where ard1.analysis_id = 107
