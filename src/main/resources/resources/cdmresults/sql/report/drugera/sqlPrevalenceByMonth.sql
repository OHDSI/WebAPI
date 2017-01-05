SELECT
  c1.concept_id                                                AS "conceptId",
  num.stratum_2                                                AS "xCalendarMonth",
  round(1000 * (1.0 * num.count_value / denom.count_value), 5) AS "yPrevalence1000Pp"
FROM
  (SELECT *
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 902) num
  INNER JOIN
  (SELECT *
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 117) denom
    ON num.stratum_2 = denom.stratum_1
  INNER JOIN
  @vocab_database_schema.concept c1
ON num.stratum_1 = CAST(c1.concept_id AS VARCHAR )
WHERE c1.concept_id = @conceptId
