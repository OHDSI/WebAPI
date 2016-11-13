SELECT
  c1.concept_id                                                AS CONCEPT_ID,
  --all rows for all concepts, but you may split by conceptid
  c1.concept_name                                              AS CONCEPT_NAME,
  num.stratum_2                                                AS X_CALENDAR_MONTH,
  -- calendar year, note, there could be blanks
  round(1000 * (1.0 * num.count_value / denom.count_value), 5) AS Y_PREVALENCE_1000PP --prevalence, per 1000 persons
FROM
  (SELECT *
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 802) num
  INNER JOIN
  (SELECT *
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 117) denom ON num.stratum_2 = denom.stratum_1
  --calendar year
  INNER JOIN @vocab_database_schema.concept c1 ON num.stratum_1 = CAST(c1.concept_id AS VARCHAR )

