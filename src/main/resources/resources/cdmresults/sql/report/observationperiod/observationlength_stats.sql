SELECT
  min(cast(ar1.stratum_1 AS INT)) * 30 AS minValue,
  max(cast(ar1.stratum_1 AS INT)) * 30 AS maxValue,
  30                                   AS intervalSize
FROM @results_database_schema.ACHILLES_analysis aa1
INNER JOIN @results_database_schema.ACHILLES_results ar1 ON aa1.analysis_id = ar1.analysis_id,
(
SELECT count_value FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 1
) denom
WHERE aa1.analysis_id = 108