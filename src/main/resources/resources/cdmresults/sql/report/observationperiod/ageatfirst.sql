SELECT
  cast(CASE WHEN isNumeric(ar1.stratum_1) = 1 THEN ar1.stratum_1 ELSE null END AS INT)                          AS intervalIndex,
  ar1.count_value                                     AS countValue,
  round(1.0 * ar1.count_value / denom.count_value, 5) AS percentValue
FROM
  (
    SELECT *
    FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 101
  ) ar1,
  (
    SELECT count_value
    FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 1
  ) denom