SELECT
  cast(CASE WHEN ar1.analysis_id = 3 THEN ar1.stratum_1 ELSE null END AS INT) - MinValue.MinValue      AS intervalIndex,
  ar1.count_value                                     AS countValue,
  round(1.0 * ar1.count_value / denom.count_value, 5) AS percentValue
FROM (SELECT *
      FROM @results_database_schema.achilles_results WHERE analysis_id = 3) ar1,
  (SELECT min(cast(CASE WHEN analysis_id = 3 THEN stratum_1 ELSE null END AS INT)) AS MinValue
   FROM @results_database_schema.achilles_results WHERE analysis_id = 3) MinValue,
  (SELECT count_value
   FROM @results_database_schema.achilles_results WHERE analysis_id = 1) denom
ORDER BY ar1.stratum_1 ASC
