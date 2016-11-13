SELECT
  c1.concept_id     AS concept_id,
  c2.concept_name   AS category,
  ard1.min_value    AS min_value,
  ard1.p10_value    AS P10_value,
  ard1.p25_value    AS P25_value,
  ard1.median_value AS median_value,
  ard1.p75_value    AS P75_value,
  ard1.p90_value    AS P90_value,
  ard1.max_value    AS max_value
FROM @results_database_schema.ACHILLES_results_dist ard1
INNER JOIN @vocab_database_schema.concept c1 ON ard1.stratum_1 = CAST(c1.concept_id AS VARCHAR )
INNER JOIN @vocab_database_schema.concept c2 ON ard1.stratum_2 = cast(c2.concept_id AS VARCHAR )
WHERE ard1.analysis_id = 1816
AND ard1.count_value > 0
