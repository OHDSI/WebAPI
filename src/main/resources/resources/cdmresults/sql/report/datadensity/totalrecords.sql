SELECT
  table_name  AS series_name,
  stratum_1   AS x_Calendar_Month,
  count_value AS y_Record_Count
FROM
  (
    SELECT
      'Visit occurrence' AS table_name,
      stratum_1,
      count_value
    FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 220
    UNION ALL
SELECT
  'Condition occurrence' AS table_name,
  stratum_1,
  count_value
FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 420
UNION ALL
SELECT
  'Death' AS table_name,
  stratum_1,
  count_value
FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 502
UNION ALL
SELECT
  'Procedure occurrence' AS table_name,
  stratum_1,
  count_value
FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 620
UNION ALL
SELECT
  'Drug exposure' AS table_name,
  stratum_1,
  count_value
FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 720
UNION ALL
SELECT
  'Observation' AS table_name,
  stratum_1,
  count_value
FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 820
UNION ALL
SELECT
  'Drug era' AS table_name,
  stratum_1,
  count_value
FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 920
UNION ALL
SELECT
  'Condition era' AS table_name,
  stratum_1,
  count_value
FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 1020
UNION ALL
SELECT
  'Observation period' AS table_name,
  stratum_1,
  count_value
FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 111
) t1
ORDER BY series_Name, CAST(stratum_1 AS INT)