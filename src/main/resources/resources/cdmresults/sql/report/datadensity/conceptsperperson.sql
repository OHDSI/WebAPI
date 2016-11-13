SELECT
  'Condition occurrence' AS category,
  ard1.minValue          AS minValue,
  ard1.p10Value          AS p10Value,
  ard1.p25Value          AS p25Value,
  ard1.medianValue       AS medianValue,
  ard1.p75Value          AS p75Value,
  ard1.p90Value          AS p90Value,
  ard1.maxValue          AS maxValue
FROM @results_database_schema.ACHILLES_results_dist ard1
WHERE ard1.analysis_id = 403

UNION

SELECT
  'Procedure occurrence' AS category,
  ard1.minValue          AS minValue,
  ard1.p10Value          AS p10Value,
  ard1.p25Value          AS p25Value,
  ard1.medianValue       AS medianValue,
  ard1.p75Value          AS p75Value,
  ard1.p90Value          AS p90Value,
  ard1.maxValue          AS maxValue
FROM @results_database_schema.ACHILLES_results_dist ard1
WHERE ard1.analysis_id = 603

UNION

SELECT
  'Drug exposure'  AS category,
  ard1.minValue    AS minValue,
  ard1.p10Value    AS p10Value,
  ard1.p25Value    AS p25Value,
  ard1.medianValue AS medianValue,
  ard1.p75Value    AS p75Value,
  ard1.p90Value    AS p90Value,
  ard1.maxValue    AS maxValue
FROM @results_database_schema.ACHILLES_results_dist ard1
WHERE ard1.analysis_id = 703

UNION

SELECT
  'Observation'    AS category,
  ard1.minValue    AS minValue,
  ard1.p10Value    AS p10Value,
  ard1.p25Value    AS p25Value,
  ard1.medianValue AS medianValue,
  ard1.p75Value    AS p75Value,
  ard1.p90Value    AS p90Value,
  ard1.maxValue    AS maxValue
FROM @results_database_schema.ACHILLES_results_dist ard1
WHERE ard1.analysis_id = 803

UNION

SELECT
  'Drug era'       AS category,
  ard1.minValue    AS minValue,
  ard1.p10Value    AS p10Value,
  ard1.p25Value    AS p25Value,
  ard1.medianValue AS medianValue,
  ard1.p75Value    AS p75Value,
  ard1.p90Value    AS p90Value,
  ard1.maxValue    AS maxValue
FROM @results_database_schema.ACHILLES_results_dist ard1
WHERE ard1.analysis_id = 903

UNION

SELECT
  'Condition era'  AS category,
  ard1.minValue    AS minValue,
  ard1.p10Value    AS p10Value,
  ard1.p25Value    AS p25Value,
  ard1.medianValue AS medianValue,
  ard1.p75Value    AS p75Value,
  ard1.p90Value    AS p90Value,
  ard1.maxValue    AS maxValue
FROM @results_database_schema.ACHILLES_results_dist ard1
WHERE ard1.analysis_id = 1003

