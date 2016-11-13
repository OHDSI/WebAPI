SELECT
  cast(cast(ard1.stratum_1 AS INT) * 10 AS VARCHAR) + '-' +
  cast((cast(ard1.stratum_1 AS INT) + 1) * 10 - 1 AS VARCHAR) AS category,
  ard1.min_value                                              AS minValue,
  ard1.p10_value                                              AS p10Value,
  ard1.p25_value                                              AS p25Value,
  ard1.median_value                                           AS medianValue,
  ard1.p75_value                                              AS p75Value,
  ard1.p90_value                                              AS p90Value,
  ard1.max_value                                              AS maxValue
FROM @results_database_schema.ACHILLES_results_dist ard1
WHERE ard1.analysis_id = 107
