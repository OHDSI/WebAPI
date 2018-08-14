SELECT c1.concept_id AS concept_id,
	CONCAT(cast(num_stratum_4 * 10 AS VARCHAR(11)), '-', cast((num_stratum_4 + 1) * 10 - 1 AS VARCHAR(11))) AS trellis_name,
	c2.concept_name AS series_name,  
	num_stratum_2 AS x_calendar_year, 
	ROUND(1000 * (1.0 * num_count_value / denom_count_value), 5) AS y_prevalence_1000pp 
FROM (
	SELECT num.stratum_1 num_stratum_1,
		CAST(num.stratum_2 AS INT) AS num_stratum_2,
		num.stratum_3 num_stratum_3,
		CAST(num.stratum_4 AS INT) AS num_stratum_4,
		num.count_value AS num_count_value,
		denom.count_value AS denom_count_value
	FROM (
		SELECT *
		FROM result_schema.achilles_results
		WHERE analysis_id = 904
			AND stratum_3 IN ('8507', '8532')
		) num
	INNER JOIN (
		SELECT *
		FROM result_schema.achilles_results
		WHERE analysis_id = 116
			AND stratum_2 IN ('8507', '8532')
		) denom
		ON num.stratum_2 = denom.stratum_1
			AND num.stratum_3 = denom.stratum_2
			AND num.stratum_4 = denom.stratum_3
	) tmp
INNER JOIN vocab_schema.concept c1
	ON num_stratum_1 = CAST(c1.concept_id as VARCHAR)
INNER JOIN vocab_schema.concept c2
	ON num_stratum_3 = CAST(c2.concept_id as VARCHAR)
WHERE c1.concept_id = ?
ORDER BY c1.concept_id,	num_stratum_2
