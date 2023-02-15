SELECT
  min(cast(CASE WHEN ar1.analysis_id = 108 THEN ar1.stratum_1 ELSE null END AS INT)) * 30 AS minValue,
  max(cast(CASE WHEN ar1.analysis_id = 108 THEN ar1.stratum_1 ELSE null END AS INT)) * 30 AS maxValue,
  30                                   AS intervalSize
FROM @results_database_schema.ACHILLES_analysis aa1
INNER JOIN @results_database_schema.achilles_results ar1 ON aa1.analysis_id = ar1.analysis_id,
(
SELECT count_value FROM @results_database_schema.achilles_results WHERE analysis_id = 1
) denom
WHERE ar1.analysis_id = 108