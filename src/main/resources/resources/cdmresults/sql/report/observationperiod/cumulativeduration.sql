SELECT
  'Length of observation'                                  AS seriesName,
  xLengthOfObservation                                     AS xLengthOfObservation,
  round(1.0 * sum(ar2.count_value) / denom.count_value, 5) AS yPercentPersons
FROM (SELECT *,
        cast(stratum_1 AS INT) * 30 AS xLengthOfObservation
      FROM @results_database_schema.achilles_results WHERE analysis_id = 108) ar1
  INNER JOIN
  (
    SELECT *
    FROM @results_database_schema.achilles_results WHERE analysis_id = 108
  ) ar2 ON ar1.analysis_id = ar2.analysis_id AND cast(ar1.stratum_1 AS INT) <= cast(ar2.stratum_1 AS INT)
  ,
  (
    SELECT count_value
    FROM @results_database_schema.achilles_results WHERE analysis_id = 1
  ) denom
GROUP BY xLengthOfObservation, denom.count_value
ORDER BY xLengthOfObservation ASC
