WITH total_exposed_w_outcome AS (
	SELECT exposure_cohort_definition_id, outcome_cohort_definition_id, count_value
	FROM result_schema.penelope_results 
	WHERE exposure_cohort_definition_id in ( ? )
		AND outcome_cohort_definition_id in ( ? )
		AND analysis_id = 1
		AND stratum_1 = 'Outcome post-exposure'
),
total_exposed AS (
	SELECT exposure_cohort_definition_id, outcome_cohort_definition_id, 
		sum(count_value) as count_value
	FROM result_schema.penelope_results 
	WHERE exposure_cohort_definition_id in ( ? )
		AND outcome_cohort_definition_id in ( ? )
		AND analysis_id = 1
		AND stratum_1 IN ('Exposure with no outcome','Outcome post-exposure')
	GROUP BY exposure_cohort_definition_id, outcome_cohort_definition_id 
),
concept_w_outcome AS (
	SELECT exposure_cohort_definition_id, outcome_cohort_definition_id,
		stratum_2 as concept_id,
		count_value as count_value
	FROM result_schema.penelope_results
	WHERE exposure_cohort_definition_id in ( ? )
		AND outcome_cohort_definition_id in ( ? )
		AND analysis_id in (1822, 1832, 1852, 1872, 1882)
		AND stratum_1 = 'Outcome post-exposure'
		AND stratum_3 = '-1'
		AND stratum_2 <> '0'
),
concept_total AS (
	SELECT exposure_cohort_definition_id, outcome_cohort_definition_id,
		stratum_2 as concept_id,
		SUM(count_value) as count_value
	FROM result_schema.penelope_results
	WHERE exposure_cohort_definition_id in ( ? )
		AND outcome_cohort_definition_id in ( ? )
		AND analysis_id in (1822, 1832, 1852, 1872, 1882)
		AND stratum_1 in ('Exposure with no outcome','Outcome post-exposure')
		AND stratum_3 = '-1'
		AND stratum_2 <> '0'
	GROUP BY exposure_cohort_definition_id, outcome_cohort_definition_id,
		stratum_2
)
SELECT total_exposed.exposure_cohort_definition_id, 
    total_exposed.outcome_cohort_definition_id,
	concept_total.concept_id,
	concept.concept_name,
	concept.domain_id,
	case when concept_w_outcome.count_value is null then 0 else concept_w_outcome.count_value end as concept_w_outcome,
	1.0*case when concept_w_outcome.count_value is null then 0 else concept_w_outcome.count_value end / total_exposed_w_outcome.count_value as pct_outcome_w_concept,
	1.0*(concept_total.count_value - case when concept_w_outcome.count_value is null then 0 else concept_w_outcome.count_value end) /(total_exposed.count_value - total_exposed_w_outcome.count_value) as pct_nooutcome_w_concept,
	case when (1.0*case when concept_w_outcome.count_value is null then 0 else concept_w_outcome.count_value end / total_exposed_w_outcome.count_value)>0 and (1.0*case when concept_w_outcome.count_value is null then 0 else concept_w_outcome.count_value end / total_exposed_w_outcome.count_value)<1 and (1.0*(concept_total.count_value - case when concept_w_outcome.count_value is null then 0 else concept_w_outcome.count_value end) /(total_exposed.count_value - total_exposed_w_outcome.count_value))>0 and (1.0*(concept_total.count_value - case when concept_w_outcome.count_value is null then 0 else concept_w_outcome.count_value end) /(total_exposed.count_value - total_exposed_w_outcome.count_value))<1 
		then abs( ((1.0*case when concept_w_outcome.count_value is null then 0 else concept_w_outcome.count_value end / total_exposed_w_outcome.count_value)-(1.0*(concept_total.count_value - case when concept_w_outcome.count_value is null then 0 else concept_w_outcome.count_value end) /(total_exposed.count_value - total_exposed_w_outcome.count_value)))/sqrt( ((1.0*case when concept_w_outcome.count_value is null then 0 else concept_w_outcome.count_value end / total_exposed_w_outcome.count_value)*(1-(1.0*case when concept_w_outcome.count_value is null then 0 else concept_w_outcome.count_value end / total_exposed_w_outcome.count_value))+(1.0*(concept_total.count_value - case when concept_w_outcome.count_value is null then 0 else concept_w_outcome.count_value end) /(total_exposed.count_value - total_exposed_w_outcome.count_value))*(1-(1.0*(concept_total.count_value - case when concept_w_outcome.count_value is null then 0 else concept_w_outcome.count_value end) /(total_exposed.count_value - total_exposed_w_outcome.count_value))))/2 ) ) 
		else 0 end as abs_std_diff
FROM concept_total
	INNER JOIN total_exposed
	ON concept_total.exposure_cohort_definition_id = total_exposed.exposure_cohort_definition_id
	AND concept_total.outcome_cohort_definition_id = total_exposed.outcome_cohort_definition_id
	LEFT JOIN total_exposed_w_outcome
	ON total_exposed.exposure_cohort_definition_id = total_exposed_w_outcome.exposure_cohort_definition_id
	AND total_exposed.outcome_cohort_definition_id = total_exposed_w_outcome.outcome_cohort_definition_id
	LEFT JOIN concept_w_outcome
	ON concept_total.exposure_cohort_definition_id = concept_w_outcome.exposure_cohort_definition_id
	AND concept_total.outcome_cohort_definition_id = concept_w_outcome.outcome_cohort_definition_id
	AND concept_total.concept_id = concept_w_outcome.concept_id
	INNER JOIN cdm_schema.concept
	ON concept_total.concept_id = cast(concept.concept_id as varchar)
WHERE concept_total.count_value > ?
ORDER BY abs_std_diff desc;
