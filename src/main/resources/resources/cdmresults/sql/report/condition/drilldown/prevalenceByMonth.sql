SELECT
  c1.concept_id                                                AS conceptId,
  c1.concept_name                                              AS conceptName,
  num.stratum_2                                                AS xCalendarMonth,
  -- calendar year, note, there could be blanks
  round(1000 * (1.0 * num.count_value / denom.count_value), 5) AS yPrevalence1000Pp --prevalence, per 1000 persons
FROM
  (SELECT
     CAST(stratum_1 AS INT) stratum_1,
     CAST(stratum_2 AS INT) stratum_2,
     count_value
   FROM
     @results_database_schema.ACHILLES_results WHERE analysis_id = 402 GROUP BY analysis_id, stratum_1, stratum_2, count_value) num
  INNER JOIN
  (SELECT
     CAST(stratum_1 AS INT) stratum_1,
     count_value
   FROM
     @results_database_schema.ACHILLES_results WHERE analysis_id = 117 GROUP BY analysis_id, stratum_1, count_value) denom
    ON num.stratum_2 = denom.stratum_1
  --calendar year
  INNER JOIN
  @vocab_database_schema.concept c1
ON num.stratum_1 = c1.concept_id
WHERE c1.concept_id = @conceptId
ORDER BY CAST(num.stratum_2 AS INT)