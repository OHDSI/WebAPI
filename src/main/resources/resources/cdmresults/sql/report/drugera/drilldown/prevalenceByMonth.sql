SELECT
  c1.concept_id                                                AS concept_id,
  num.stratum_2                                                AS x_calendar_month,
  round(1000 * (1.0 * num.count_value / denom.count_value), 5) AS y_prevalence_1000_pp
FROM
  (SELECT *
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 902) num
  INNER JOIN
  (SELECT *
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 117) denom
    ON num.stratum_2 = denom.stratum_1
  INNER JOIN
  @vocab_database_schema.concept c1
ON CAST(CASE WHEN isNumeric(num.stratum_1) = 1 THEN num.stratum_1 ELSE null END AS INT) = c1.concept_id
WHERE c1.concept_id = @conceptId
