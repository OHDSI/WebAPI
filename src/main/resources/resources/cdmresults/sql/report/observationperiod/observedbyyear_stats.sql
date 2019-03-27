SELECT
  min(cast(CASE WHEN isNumeric(ar1.stratum_1) = 1 THEN ar1.stratum_1 ELSE null END AS INT)) AS minValue,
  max(cast(CASE WHEN isNumeric(ar1.stratum_1) = 1 THEN ar1.stratum_1 ELSE null END AS INT)) AS maxValue,
  1                               AS intervalSize
FROM @results_database_schema.achilles_results ar1
WHERE ar1.analysis_id = 109