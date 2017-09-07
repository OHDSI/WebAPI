SELECT
  cast(cast(num.stratum_3 AS INT) * 10 AS VARCHAR) + '-' +
  cast((cast(num.stratum_3 AS INT) + 1) * 10 - 1 AS VARCHAR)   AS trellis_Name,
  --age decile
  c2.concept_name                                              AS series_Name,
  --gender
  num.stratum_1                                                AS x_Calendar_Year,
  -- calendar year, note, there could be blanks
  ROUND(1000 * (1.0 * num.count_value / denom.count_value), 5) AS y_Prevalence_1000Pp --prevalence, per 1000 persons
FROM
  (SELECT *
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 504) num
  INNER JOIN
  (SELECT *
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 116) denom
    ON num.stratum_1 = denom.stratum_1  --calendar year
       AND num.stratum_2 = denom.stratum_2 --gender
       AND num.stratum_3 = denom.stratum_3
  --age decile
  INNER JOIN @vocab_database_schema.concept c2 ON num.stratum_2 = CAST(c2.concept_id AS VARCHAR )
WHERE c2.concept_id IN (8507, 8532)

