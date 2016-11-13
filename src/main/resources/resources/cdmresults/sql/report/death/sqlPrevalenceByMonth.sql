SELECT
  num.stratum_1                                      AS xCalendarMonth,
  -- calendar year, note, there could be blanks
  1000 * (1.0 * num.count_value / denom.count_value) AS yPrevalence1000Pp --prevalence, per 1000 persons
FROM
  (SELECT *
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 502) num
  INNER JOIN
  (SELECT *
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 117) denom
    ON num.stratum_1 = denom.stratum_1  --calendar year

