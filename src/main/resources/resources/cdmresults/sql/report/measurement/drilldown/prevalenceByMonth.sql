SELECT
  c1.concept_id                                                AS conceptId,
  --all rows for all concepts, but you may split by conceptid
  c1.concept_name                                              AS conceptName,
  num.stratum_2                                                AS xCalendarMonth,
  -- calendar year, note, there could be blanks
  round(1000 * (1.0 * num.count_value / denom.count_value), 5) AS yPrevalence1000Pp --prevalence, per 1000 persons
FROM
  (SELECT *
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 1802) num
  INNER JOIN
  (SELECT *
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 117) denom ON num.stratum_2 = denom.stratum_1
  --calendar year
  INNER JOIN @vocab_database_schema.concept c1 ON num.stratum_1 = CAST(c1.concept_id AS VARCHAR )
WHERE c1.concept_id = @conceptId

