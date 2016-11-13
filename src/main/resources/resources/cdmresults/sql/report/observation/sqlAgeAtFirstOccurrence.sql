SELECT
  c1.concept_id     AS CONCEPT_ID,
  c2.concept_name   AS CATEGORY,
  ard1.min_value    AS MIN_VALUE,
  ard1.p10_value    AS P10_VALUE,
  ard1.p25_value    AS P25_VALUE,
  ard1.median_value AS MEDIAN_VALUE,
  ard1.p75_value    AS P75_VALUE,
  ard1.p90_value    AS P90_VALUE,
  ard1.max_value    AS MAX_VALUE
FROM @results_database_schema.ACHILLES_results_dist ard1
INNER JOIN @vocab_database_schema.concept c1 ON ard1.stratum_1 = CAST(c1.concept_id AS VARCHAR )
INNER JOIN @vocab_database_schema.concept c2 ON ard1.stratum_2 = CAST(c2.concept_id AS VARCHAR )
WHERE ard1.analysis_id = 806
