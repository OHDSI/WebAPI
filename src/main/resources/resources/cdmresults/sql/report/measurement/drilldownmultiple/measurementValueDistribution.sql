select c2.concept_name   AS category,
       avg(ard1.min_value) AS min_value,
       avg(ard1.p10_value) AS p10_value,
       avg(ard1.p25_value) AS p25_value,
       avg(ard1.median_value) AS median_value,
       avg(ard1.p75_value) AS p75_value,
       avg(ard1.p90_value) AS p90_value,
       avg(ard1.max_value) AS max_value
from (
       select cast(CASE WHEN isNumeric(stratum_1) = 1 THEN stratum_1 ELSE null END as int) stratum_1, 
              cast(CASE WHEN isNumeric(stratum_2) = 1 THEN stratum_2 ELSE null END as int) stratum_2, 
              min_value, p10_value, p25_value, median_value, p75_value, p90_value, max_value
       FROM @results_database_schema.achilles_results_dist
       where analysis_id = 1815 and count_value > 0
       GROUP BY analysis_id, stratum_1, stratum_2, min_value, p10_value, p25_value, median_value, p75_value, p90_value, max_value
     ) ard1
  inner join @vocab_database_schema.concept c1 on ard1.stratum_1 = c1.concept_id
  inner join @vocab_database_schema.concept c2 on ard1.stratum_2 = c2.concept_id
where
  c1.concept_id in (@conceptIds)
GROUP BY c2.concept_name