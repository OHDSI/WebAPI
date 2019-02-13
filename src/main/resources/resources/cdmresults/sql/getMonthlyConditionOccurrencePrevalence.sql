SELECT
  num.stratum_2                                                AS x_calendar_month,
  round(1000 * (1.0 * num.count_value / denom.count_value), 5) AS y_prevalence_1000pp
FROM (
       SELECT
         stratum_1,
         stratum_2,
         count_value
       FROM @OHDSI_schema.achilles_results
       WHERE analysis_id = 402 AND stratum_1 = '@conceptId'
) num
INNER JOIN (
	SELECT
		 stratum_1,
		 count_value
	 FROM @OHDSI_schema.achilles_results
	 WHERE analysis_id = 117
) denom ON num.stratum_2 = denom.stratum_1
ORDER BY cast(num.stratum_2 AS INT)