WITH denominator AS (
	
	SELECT t1.exposure_cohort_definition_id, t1.outcome_cohort_definition_id,
		-30*t1.stratum_1 AS duration,
		SUM(t1.count_value) OVER (PARTITION BY t1.exposure_cohort_definition_id, t1.outcome_cohort_definition_id ORDER BY -30*t1.stratum_1 ASC) AS count_value
	FROM
	(
		SELECT exposure_cohort_definition_id, outcome_cohort_definition_id, stratum_1, max(count_value) as count_value
		FROM
		(
			SELECT exposure_cohort_definition_id, 
				outcome_cohort_definition_id,
				ROW_NUMBER() OVER (ORDER BY analysis_id) AS stratum_1, 0 AS count_value
			FROM result_schema.penelope_results
			WHERE exposure_cohort_definition_id in ( ?,? )
			AND outcome_cohort_definition_id in ( ?,? )
			AND analysis_id = 1805

			UNION

			SELECT exposure_cohort_definition_id, 
				outcome_cohort_definition_id,
				cast(stratum_2 AS INTEGER) as stratum_1, sum(count_value) as count_value
			FROM result_schema.penelope_results
			WHERE exposure_cohort_definition_id in ( ?,? )
			AND outcome_cohort_definition_id in ( ?,? )
			AND analysis_id = 1805
			GROUP BY exposure_cohort_definition_id, 
				outcome_cohort_definition_id,
				cast(stratum_2 AS INTEGER)
		) t0
		WHERE CAST(stratum_1 AS INTEGER)<= 10 OR count_value > 0
		GROUP BY exposure_cohort_definition_id, outcome_cohort_definition_id, stratum_1 
	) t1

	UNION

	

	SELECT t1.exposure_cohort_definition_id, t1.outcome_cohort_definition_id,
		30*t1.stratum_1 AS duration,
		total.count_value - SUM(t1.count_value) OVER (PARTITION BY t1.exposure_cohort_definition_id, t1.outcome_cohort_definition_id ORDER BY 30*t1.stratum_1 ASC) AS count_value
	FROM
	(
		SELECT exposure_cohort_definition_id, outcome_cohort_definition_id, stratum_1, max(count_value) as count_value
		FROM
		(
			SELECT exposure_cohort_definition_id, 
				outcome_cohort_definition_id,
				ROW_NUMBER() OVER (ORDER BY analysis_id) AS stratum_1, 0 AS count_value
			FROM result_schema.penelope_results
			WHERE exposure_cohort_definition_id in ( ?,? )
			AND outcome_cohort_definition_id in ( ?,? )
			AND analysis_id = 1806

			UNION

			SELECT exposure_cohort_definition_id, 
				outcome_cohort_definition_id,
				cast(stratum_2 AS INTEGER) as stratum_1, sum(count_value) as count_value
			FROM result_schema.penelope_results
			WHERE exposure_cohort_definition_id in ( ?,? )
			AND outcome_cohort_definition_id in ( ?,? )
			AND analysis_id = 1806
			GROUP BY exposure_cohort_definition_id, 
				outcome_cohort_definition_id,
				cast(stratum_2 AS INTEGER)
		) t0
		WHERE CAST(stratum_1 AS INTEGER)<= 10 OR count_value > 0
		GROUP BY exposure_cohort_definition_id, outcome_cohort_definition_id, stratum_1 
	) t1
	INNER JOIN
	(
		SELECT exposure_cohort_definition_id, 
				outcome_cohort_definition_id,
				sum(count_value) as count_value
			FROM result_schema.penelope_results
			WHERE exposure_cohort_definition_id in ( ?,? )
			AND outcome_cohort_definition_id in ( ?,? )
			AND analysis_id = 1
			GROUP BY exposure_cohort_definition_id, 
				outcome_cohort_definition_id
	) total
	ON t1.exposure_cohort_definition_id = total.exposure_cohort_definition_id
	AND t1.outcome_cohort_definition_id = total.outcome_cohort_definition_id
),
numerator_first as
(
	SELECT exposure_cohort_definition_id, 
		outcome_cohort_definition_id,
		CAST(stratum_1 AS INTEGER)*30 AS duration, 
		count_value
	FROM result_schema.penelope_results
	WHERE exposure_cohort_definition_id in ( ?,? )
	AND outcome_cohort_definition_id in ( ?,? )
	AND analysis_id = 12
),
numerator_all as
(
	SELECT exposure_cohort_definition_id, 
		outcome_cohort_definition_id,
		CAST(stratum_1 AS INTEGER)*30 AS duration, 
		count_value
	FROM result_schema.penelope_results
	WHERE exposure_cohort_definition_id in ( ?,? )
	AND outcome_cohort_definition_id in ( ?,? )
	AND analysis_id = 13
)
SELECT denominator.exposure_cohort_definition_id,
	denominator.outcome_cohort_definition_id,
	'All' as record_type,
	denominator.duration,
	case when numerator.count_value is null then 0 else numerator.count_value end as count_value,
	case when denominator.count_value > 0 then 1.0*case when numerator.count_value is null then 0 else numerator.count_value end / denominator.count_value else 0 end as pct_persons
FROM denominator
LEFT JOIN
numerator_all numerator
ON denominator.exposure_cohort_definition_id = numerator.exposure_cohort_definition_id
AND denominator.outcome_cohort_definition_id = numerator.outcome_cohort_definition_id
AND denominator.duration = numerator.duration
WHERE denominator.duration between -1000 and 1000

UNION

SELECT denominator.exposure_cohort_definition_id,
	denominator.outcome_cohort_definition_id,
	'First' as record_type,
	denominator.duration,
	case when numerator.count_value is null then 0 else numerator.count_value end as count_value,
	case when denominator.count_value > 0 then 1.0*case when numerator.count_value is null then 0 else numerator.count_value end / denominator.count_value else 0 end as pct_persons
FROM denominator
LEFT JOIN
numerator_first numerator
ON denominator.exposure_cohort_definition_id = numerator.exposure_cohort_definition_id
AND denominator.outcome_cohort_definition_id = numerator.outcome_cohort_definition_id
AND denominator.duration = numerator.duration
WHERE denominator.duration between -1000 and 1000
;
