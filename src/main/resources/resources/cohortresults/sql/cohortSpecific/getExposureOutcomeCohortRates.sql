SELECT total.exposure_cohort_definition_id,
	total.outcome_cohort_definition_id,
	total.count_value as num_persons_exposed,
	case when outcomepreexposure.count_value is not null then outcomepreexposure.count_value else 0 end as num_persons_w_outcome_pre_exposure,
	case when outcomepostexposure.count_value is not null then outcomepostexposure.count_value else 0 end as num_persons_w_outcome_post_exposure,
	(case when timeatrisk.time_at_risk is not null then timeatrisk.time_at_risk else 0.1 end) as time_at_risk,
	1000.0*(case when outcomepostexposure.count_value is not null then outcomepostexposure.count_value else 0 end)/(case when timeatrisk.time_at_risk is not null then timeatrisk.time_at_risk else 0.1 end) as incidence_rate_1000py
FROM
(
    SELECT exposure_cohort_definition_id, outcome_cohort_definition_id, count_value
    FROM @ohdsi_database_schema.penelope_results
    WHERE exposure_cohort_definition_id = @exposure_cohort_definition_id
    AND outcome_cohort_definition_id = @outcome_cohort_definition_id
    AND analysis_id = 0
) total
LEFT JOIN
(
    SELECT exposure_cohort_definition_id, outcome_cohort_definition_id, count_value
    FROM @ohdsi_database_schema.penelope_results
    WHERE exposure_cohort_definition_id = @exposure_cohort_definition_id
    AND outcome_cohort_definition_id = @outcome_cohort_definition_id
    AND analysis_id = 1
    AND stratum_1 = 'Outcome pre-exposure'
) outcomepreexposure
ON total.exposure_cohort_definition_id = outcomepreexposure.exposure_cohort_definition_id
AND total.outcome_cohort_definition_id = outcomepreexposure.outcome_cohort_definition_id
LEFT JOIN
(
    SELECT exposure_cohort_definition_id, outcome_cohort_definition_id, count_value
    FROM @ohdsi_database_schema.penelope_results
    WHERE exposure_cohort_definition_id = @exposure_cohort_definition_id
    AND outcome_cohort_definition_id = @outcome_cohort_definition_id
    AND analysis_id = 1
    AND stratum_1 = 'Outcome post-exposure'
) outcomepostexposure
ON total.exposure_cohort_definition_id = outcomepostexposure.exposure_cohort_definition_id
AND total.outcome_cohort_definition_id = outcomepostexposure.outcome_cohort_definition_id
LEFT JOIN
(
    SELECT exposure_cohort_definition_id, outcome_cohort_definition_id, sum(count_value) as time_at_risk
    FROM @ohdsi_database_schema.penelope_results
    WHERE exposure_cohort_definition_id = @exposure_cohort_definition_id
    AND outcome_cohort_definition_id = @outcome_cohort_definition_id
    AND analysis_id = 10
    AND stratum_1 in ('Outcome post-exposure', 'Exposure with no outcome')
    GROUP BY exposure_cohort_definition_id, outcome_cohort_definition_id
) timeatrisk
ON total.exposure_cohort_definition_id = timeatrisk.exposure_cohort_definition_id
AND total.outcome_cohort_definition_id = timeatrisk.outcome_cohort_definition_id;