SELECT
  CONCAT(
    cast(cast(CASE WHEN num.analysis_id = 504 THEN num.stratum_3 ELSE null END AS INT) * 10 AS VARCHAR(11)), '-',
    cast((cast(CASE WHEN num.analysis_id = 504 THEN num.stratum_3 ELSE null END AS INT) + 1) * 10 - 1 AS VARCHAR(11))
  ) AS trellis_Name,
  --age decile
  c2.concept_name                                              AS series_Name,
  --gender
  num.stratum_1                                                AS x_Calendar_Year,
  -- calendar year, note, there could be blanks
  ROUND(1000 * (1.0 * num.count_value / denom.count_value), 5) AS y_Prevalence_1000Pp --prevalence, per 1000 persons
FROM
  (SELECT analysis_id, stratum_1, stratum_2, stratum_3, count_value
		FROM @results_database_schema.achilles_results
		WHERE analysis_id = 504
		GROUP BY analysis_id, stratum_1, stratum_2, stratum_3, count_value
	) num
  INNER JOIN (
		SELECT stratum_1, stratum_2, stratum_3, count_value
		FROM @results_database_schema.achilles_results
		WHERE analysis_id = 116
		GROUP BY stratum_1, stratum_2, stratum_3, count_value
	) denom ON num.stratum_1 = denom.stratum_1  --calendar year
       AND num.stratum_2 = denom.stratum_2 --gender
       AND num.stratum_3 = denom.stratum_3
  --age decile
  INNER JOIN @vocab_database_schema.concept c2 ON CAST(CASE WHEN num.analysis_id = 504 THEN num.stratum_2 ELSE null END AS BIGINT) = c2.concept_id
WHERE c2.concept_id IN (8507, 8532)

