SELECT
  c1.concept_id                                      AS concept_id,
  --all rows for all concepts, but you may split by conceptid
  c1.concept_name                                    AS concept_name,
  num.stratum_2                                      AS x_calendar_month,
  -- calendar year, note, there could be blanks
  1000 * (1.0 * num.count_value / denom.count_value) AS y_prevalence_1000_pp --prevalence, per 1000 persons
FROM
  (SELECT *
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 202) num
  INNER JOIN
  (SELECT *
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 117) denom ON num.stratum_2 = denom.stratum_1
  --calendar year
  INNER JOIN @vocab_database_schema.concept c1 ON CAST(CASE WHEN isNumeric(num.stratum_1) = 1 THEN num.stratum_1 ELSE null END AS INT) = c1.concept_id
WHERE c1.concept_id = @conceptId

