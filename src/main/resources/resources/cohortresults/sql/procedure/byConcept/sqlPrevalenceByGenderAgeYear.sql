SELECT c1.concept_id AS concept_id,
	c1.concept_name as concept_name,
	CONCAT(cast(CAST(CASE WHEN isNumeric(num_stratum_4) = 1 THEN num_stratum_4 ELSE null END AS INT) * 10 AS VARCHAR(11)),
         '-', cast((CAST(CASE WHEN isNumeric(num_stratum_4) = 1 THEN num_stratum_4 ELSE null END AS INT) + 1) * 10 - 1 AS
                                                                VARCHAR(11))) AS trellis_name, --age decile
	c2.concept_name AS series_name,  --gender
	CAST(CASE WHEN isNumeric(num_stratum_2) = 1 THEN num_stratum_2 ELSE null END AS INT) AS x_calendar_year, -- calendar year, note, there could be blanks
	ROUND(1000 * (1.0 * num_count_value / denom_count_value), 5) AS y_prevalence_1000pp --prevalence, per 1000 persons
FROM (
	SELECT num.stratum_1 AS num_stratum_1,
		num.stratum_2 AS num_stratum_2,
		num.stratum_3 AS num_stratum_3,
		num.stratum_4 num_stratum_4,
		num.count_value AS num_count_value,
		denom.count_value AS denom_count_value
	FROM (
		SELECT stratum_1, stratum_2, stratum_3, stratum_4, count_value
		FROM @ohdsi_database_schema.heracles_results
		WHERE analysis_id = 604
			AND stratum_3 IN ('8507', '8532')
			and cohort_definition_id = @cohortDefinitionId
		GROUP BY stratum_1, stratum_2, stratum_3, stratum_4, count_value
		) num
	INNER JOIN (
		SELECT stratum_1, stratum_2, stratum_3, count_value
		FROM @ohdsi_database_schema.heracles_results
		WHERE analysis_id = 116
			AND stratum_2 IN ('8507', '8532')
			and cohort_definition_id = @cohortDefinitionId
		GROUP BY stratum_1, stratum_2, stratum_3, count_value
		) denom ON num.stratum_2 = denom.stratum_1
			AND num.stratum_3 = denom.stratum_2
			AND num.stratum_4 = denom.stratum_3
	) tmp
INNER JOIN @cdm_database_schema.concept c1 ON CAST(CASE WHEN isNumeric(num_stratum_1) = 1 THEN num_stratum_1 ELSE null END AS INT) = c1.concept_id
INNER JOIN @cdm_database_schema.concept c2 ON CAST(CASE WHEN isNumeric(num_stratum_3) = 1 THEN num_stratum_3 ELSE null END AS INT) = c2.concept_id
where  c1.concept_id = @conceptId
ORDER BY c1.concept_id,	num_stratum_2
