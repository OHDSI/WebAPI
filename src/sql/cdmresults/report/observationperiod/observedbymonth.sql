SELECT
  cast(CASE WHEN ar1.analysis_id = 110 THEN ar1.stratum_1 ELSE null END AS INT)                          AS monthYear,
  ar1.count_value                                     AS countValue,
  round(1.0 * ar1.count_value / denom.count_value, 5) AS percentValue
FROM (SELECT *
      FROM @results_database_schema.achilles_results WHERE analysis_id = 110) ar1,
  (SELECT count_value
   FROM @results_database_schema.achilles_results WHERE analysis_id = 1) denom

  