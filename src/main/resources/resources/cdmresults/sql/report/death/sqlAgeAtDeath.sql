SELECT
  c2.concept_name   AS category,
  ard1.min_value    AS min_Value,
  ard1.p10_value    AS p10_Value,
  ard1.p25_value    AS p25_Value,
  ard1.median_value AS median_Value,
  ard1.p75_value    AS p75_Value,
  ard1.p90_value    AS p90_Value,
  ard1.max_value    AS max_Value
FROM @results_database_schema.ACHILLES_results_dist ard1
INNER JOIN
@vocab_database_schema.concept c2 ON ard1.stratum_1 = CAST(c2.concept_id AS VARCHAR )
WHERE ard1.analysis_id = 506