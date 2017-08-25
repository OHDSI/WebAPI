SELECT
  min(cast(ar1.stratum_1 AS INT)) AS minValue,
  max(cast(ar1.stratum_1 AS INT)) AS maxValue,
  1                               AS intervalSize
FROM @results_database_schema.ACHILLES_results ar1
WHERE ar1.analysis_id = 3
