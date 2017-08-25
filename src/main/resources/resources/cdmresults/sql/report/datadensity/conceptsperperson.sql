SELECT
  'Condition occurrence' AS category,
  ard1.min_Value          AS min_Value,
  ard1.p10_Value          AS p10_Value,
  ard1.p25_Value          AS p25_Value,
  ard1.median_Value       AS median_Value,
  ard1.p75_Value          AS p75_Value,
  ard1.p90_Value          AS p90_Value,
  ard1.max_Value          AS max_Value
FROM @results_database_schema.ACHILLES_results_dist ard1
WHERE ard1.analysis_id = 403

UNION

SELECT
  'Procedure occurrence' AS category,
  ard1.min_Value          AS min_Value,
  ard1.p10_Value          AS p10_Value,
  ard1.p25_Value          AS p25_Value,
  ard1.median_Value       AS median_Value,
  ard1.p75_Value          AS p75_Value,
  ard1.p90_Value          AS p90_Value,
  ard1.max_Value          AS max_Value
FROM @results_database_schema.ACHILLES_results_dist ard1
WHERE ard1.analysis_id = 603

UNION

SELECT
  'Drug exposure'  AS category,
  ard1.min_Value    AS min_Value,
  ard1.p10_Value    AS p10_Value,
  ard1.p25_Value    AS p25_Value,
  ard1.median_Value AS median_Value,
  ard1.p75_Value    AS p75_Value,
  ard1.p90_Value    AS p90_Value,
  ard1.max_Value    AS max_Value
FROM @results_database_schema.ACHILLES_results_dist ard1
WHERE ard1.analysis_id = 703

UNION

SELECT
  'Observation'    AS category,
  ard1.min_Value    AS min_Value,
  ard1.p10_Value    AS p10_Value,
  ard1.p25_Value    AS p25_Value,
  ard1.median_Value AS median_Value,
  ard1.p75_Value    AS p75_Value,
  ard1.p90_Value    AS p90_Value,
  ard1.max_Value    AS max_Value
FROM @results_database_schema.ACHILLES_results_dist ard1
WHERE ard1.analysis_id = 803

UNION

SELECT
  'Drug era'       AS category,
  ard1.min_Value    AS min_Value,
  ard1.p10_Value    AS p10_Value,
  ard1.p25_Value    AS p25_Value,
  ard1.median_Value AS median_Value,
  ard1.p75_Value    AS p75_Value,
  ard1.p90_Value    AS p90_Value,
  ard1.max_Value    AS max_Value
FROM @results_database_schema.ACHILLES_results_dist ard1
WHERE ard1.analysis_id = 903

UNION

SELECT
  'Condition era'  AS category,
  ard1.min_Value    AS min_Value,
  ard1.p10_Value    AS p10_Value,
  ard1.p25_Value    AS p25_Value,
  ard1.median_Value AS median_Value,
  ard1.p75_Value    AS p75_Value,
  ard1.p90_Value    AS p90_Value,
  ard1.max_Value    AS max_Value
FROM @results_database_schema.ACHILLES_results_dist ard1
WHERE ard1.analysis_id = 1003

