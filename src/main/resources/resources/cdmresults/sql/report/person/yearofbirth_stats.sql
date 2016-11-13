SELECT
  min(cast(ar1.stratum_1 AS INT)) AS min_value,
  max(cast(ar1.stratum_1 AS INT)) AS max_value,
  1                               AS interval_size
FROM @results_database_schema.ACHILLES_results ar1
WHERE ar1.analysis_id = 3
