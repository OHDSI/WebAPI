SELECT
  cast(ar1.stratum_1 AS INT)                          AS interval_index,
  ar1.count_value                                     AS count_value,
  round(1.0 * ar1.count_value / denom.count_value, 5) AS percent_value
FROM @results_database_schema.ACHILLES_analysis aa1
INNER JOIN @results_database_schema.ACHILLES_results ar1 ON aa1.analysis_id = ar1.analysis_id,
(
SELECT count_value FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 1
) denom
WHERE aa1.analysis_id = 108
