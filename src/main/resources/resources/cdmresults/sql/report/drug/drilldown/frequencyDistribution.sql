SELECT 
	c1.concept_id								AS concept_id,
	c1.concept_name							AS concept_name,
	CAST(ROUND((100.0*num.count_value / denom.count_value), 0) AS INT) AS y_num_persons,
	num.stratum_2								AS x_count
FROM 
	(SELECT count_value FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 1) denom,
	(SELECT CAST(CASE WHEN isNumeric(stratum_1) = 1 THEN stratum_1 ELSE null END AS INT) stratum_1, 
	        CAST(CASE WHEN isNumeric(stratum_2) = 1 THEN stratum_2 ELSE null END AS INT) stratum_2, count_value 
		FROM @results_database_schema.ACHILLES_results 
		WHERE analysis_id = 791) num
	INNER JOIN @vocab_database_schema.concept c1 ON num.stratum_1 = c1.concept_id
WHERE c1.concept_id = @conceptId
