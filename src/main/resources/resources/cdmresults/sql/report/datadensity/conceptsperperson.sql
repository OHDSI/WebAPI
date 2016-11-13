SELECT
  'Condition occurrence' AS Category,
  ard1.min_value         AS min_value,
  ard1.p10_value         AS p10_value,
  ard1.p25_value         AS p25_value,
  ard1.median_value      AS median_value,
  ard1.p75_value         AS p75_value,
  ard1.p90_value         AS p90_value,
  ard1.max_value         AS max_value
FROM @results_database_schema.ACHILLES_results_dist ard1
WHERE ard1.analysis_id = 403

UNION

SELECT
  'Procedure occurrence' AS Category,
  ard1.min_value         AS min_value,
  ard1.p10_value         AS p10_value,
  ard1.p25_value         AS p25_value,
  ard1.median_value      AS median_value,
  ard1.p75_value         AS p75_value,
  ard1.p90_value         AS p90_value,
  ard1.max_value         AS max_value
FROM @results_database_schema.ACHILLES_results_dist ard1
WHERE ard1.analysis_id = 603

UNION

SELECT
  'Drug exposure'   AS Category,
  ard1.min_value    AS min_value,
  ard1.p10_value    AS p10_value,
  ard1.p25_value    AS p25_value,
  ard1.median_value AS median_value,
  ard1.p75_value    AS p75_value,
  ard1.p90_value    AS p90_value,
  ard1.max_value    AS max_value
FROM @results_database_schema.ACHILLES_results_dist ard1
WHERE ard1.analysis_id = 703

UNION

SELECT
  'Observation'     AS Category,
  ard1.min_value    AS min_value,
  ard1.p10_value    AS p10_value,
  ard1.p25_value    AS p25_value,
  ard1.median_value AS median_value,
  ard1.p75_value    AS p75_value,
  ard1.p90_value    AS p90_value,
  ard1.max_value    AS max_value
FROM @results_database_schema.ACHILLES_results_dist ard1
WHERE ard1.analysis_id = 803

UNION

SELECT
  'Drug era'        AS Category,
  ard1.min_value    AS min_value,
  ard1.p10_value    AS p10_value,
  ard1.p25_value    AS p25_value,
  ard1.median_value AS median_value,
  ard1.p75_value    AS p75_value,
  ard1.p90_value    AS p90_value,
  ard1.max_value    AS max_value
FROM @results_database_schema.ACHILLES_results_dist ard1
WHERE ard1.analysis_id = 903

UNION

SELECT
  'Condition era'   AS Category,
  ard1.min_value    AS min_value,
  ard1.p10_value    AS p10_value,
  ard1.p25_value    AS p25_value,
  ard1.median_value AS median_value,
  ard1.p75_value    AS p75_value,
  ard1.p90_value    AS p90_value,
  ard1.max_value    AS max_value
FROM @results_database_schema.ACHILLES_results_dist ard1
WHERE ard1.analysis_id = 1003

