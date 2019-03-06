SELECT
  c1.concept_name   AS category,
  ard1.min_value    AS minValue,
  ard1.p10_value    AS p10Value,
  ard1.p25_value    AS p25Value,
  ard1.median_value AS medianValue,
  ard1.p75_value    AS p75Value,
  ard1.p90_value    AS p90Value,
  ard1.max_value    AS maxValue
FROM @results_database_schema.ACHILLES_results_dist ard1
INNER JOIN @vocab_database_schema.concept c1 ON CAST(CASE WHEN isNumeric(ard1.stratum_1) = 1 THEN ard1.stratum_1 ELSE null END AS INT) = c1.concept_id
WHERE ard1.analysis_id = 104