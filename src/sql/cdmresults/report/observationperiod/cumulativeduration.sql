SELECT
  'Length of observation'                                  AS seriesName,
  xLengthOfObservation                                     AS xLengthOfObservation,
  round(1.0 * sum(ar2.count_value) / denom.count_value, 5) AS yPercentPersons
FROM (SELECT ar.*,
        cast(CASE WHEN analysis_id = 108 THEN stratum_1 ELSE null END AS INT) * 30 AS xLengthOfObservation
      FROM @results_database_schema.achilles_results ar WHERE analysis_id = 108) ar1
  INNER JOIN
  (
    SELECT *
    FROM @results_database_schema.achilles_results WHERE analysis_id = 108
  ) ar2 ON ar1.analysis_id = ar2.analysis_id AND cast(CASE WHEN ar1.analysis_id = 108 THEN 
                                            ar1.stratum_1 ELSE null END AS INT) <= cast(CASE WHEN ar2.analysis_id = 108 THEN ar2.stratum_1 ELSE null END AS INT)
  ,
  (
    SELECT count_value
    FROM @results_database_schema.achilles_results WHERE analysis_id = 1
  ) denom
GROUP BY xLengthOfObservation, denom.count_value
ORDER BY xLengthOfObservation ASC
