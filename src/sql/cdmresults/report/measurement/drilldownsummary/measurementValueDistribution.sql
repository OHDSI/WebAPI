select c1.concept_id as concept_id,
       c2.concept_name as category,
       ard1.min_value as min_value,
       ard1.p10_value as P10_value,
       ard1.p25_value as P25_value,
       ard1.median_value as median_value,
       ard1.p75_value as P75_value,
       ard1.p90_value as P90_value,
       ard1.max_value as max_value
from (
       select cast(CASE WHEN analysis_id = 1815 THEN stratum_1 ELSE null END as int) stratum_1, 
              cast(CASE WHEN analysis_id = 1815 THEN stratum_2 ELSE null END as int) stratum_2, 
              min_value, p10_value, p25_value, median_value, p75_value, p90_value, max_value
       FROM @results_database_schema.achilles_results_dist
       where analysis_id = 1815 and count_value > 0
       GROUP BY analysis_id, stratum_1, stratum_2, min_value, p10_value, p25_value, median_value, p75_value, p90_value, max_value
     ) ard1
  inner join @vocab_database_schema.concept c1 on ard1.stratum_1 = c1.concept_id
  inner join @vocab_database_schema.concept c2 on ard1.stratum_2 = c2.concept_id
