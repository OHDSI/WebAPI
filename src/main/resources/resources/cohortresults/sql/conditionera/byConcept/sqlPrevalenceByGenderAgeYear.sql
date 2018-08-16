SELECT c1.concept_id AS concept_id,
	CONCAT(cast(CAST(num_stratum_4 as INT) * 10 AS VARCHAR(11)), '-', cast((CAST(num_stratum_4 as INT) + 1) * 10 - 1 AS VARCHAR(11))) AS trellis_name, --age decile
	c2.concept_name AS series_name,  --gender
	num_stratum_2 AS x_calendar_year, -- calendar year, note, there could be blanks
	ROUND(1000 * (1.0 * num_count_value / denom_count_value), 5) AS y_prevalence_1000pp --prevalence, per 1000 persons
FROM (
	SELECT CAST(num.stratum_1 AS BIGINT) AS num_stratum_1,
		num.stratum_2 AS num_stratum_2,
		CAST(num.stratum_3 AS BIGINT) AS num_stratum_3,
		num.stratum_4 AS num_stratum_4,
		num.count_value AS num_count_value,
		denom.count_value AS denom_count_value
	FROM (
		SELECT stratum_1, stratum_2, stratum_3, stratum_4, count_value
		FROM @ohdsi_database_schema.heracles_results
		WHERE analysis_id = 1004
			AND stratum_3 IN ('8507', '8532')
			AND cohort_definition_id = @cohortDefinitionId
		GROUP BY stratum_1, stratum_2, stratum_3, stratum_4, count_value
	) num
	INNER JOIN (
		SELECT stratum_1, stratum_2, stratum_3, count_value
		FROM @ohdsi_database_schema.heracles_results
		WHERE analysis_id = 116
			AND stratum_2 IN ('8507', '8532')
			AND cohort_definition_id = @cohortDefinitionId
		GROUP BY stratum_1, stratum_2, stratum_3, count_value
	) denom ON num.stratum_2 = denom.stratum_1
			AND num.stratum_3 = denom.stratum_2
			AND num.stratum_4 = denom.stratum_3
) tmp
INNER JOIN @cdm_database_schema.concept c1 ON num_stratum_1 = c1.concept_id
INNER JOIN @cdm_database_schema.concept c2 ON num_stratum_3 = c2.concept_id
WHERE c1.concept_id = @conceptId
ORDER BY c1.concept_id,	num_stratum_2

