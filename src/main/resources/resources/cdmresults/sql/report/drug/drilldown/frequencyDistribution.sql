SELECT 
	c1.concept_id								AS "conceptId",
	c1.concept_name								AS "conceptName",
	ROUND(CAST(num.stratum_2 AS DECIMAL), 2)	AS "yNumPersons",
	num.count_value								AS "xCount"
FROM 
	(SELECT stratum_1, stratum_2, count_value 
		FROM @results_database_schema.ACHILLES_results 
		WHERE analysis_id = 791) num
	INNER JOIN @vocab_database_schema.concept c1 ON CAST(num.stratum_1 AS INT) = c1.concept_id
WHERE c1.concept_id = @conceptId