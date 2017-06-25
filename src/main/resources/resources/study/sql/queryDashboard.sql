SELECT
	d.target_cohort_definition_id
	, t.short_name target_cohort_name
	, d.outcome_cohort_definition_id
	, o.short_name outcome_cohort_name
	, d.outcome_concept_id
	, c.concept_name 
	, d.incidence
	, d.estimate
	, ISNULL(s.seriousness, 99999) seriousness
        , CASE WHEN p.outcome_cohort_definition_id IS NULL THEN 0 ELSE 1 END on_label
        , CASE WHEN nc.nc_cohort_definition_id IS NULL THEN 0 ELSE 1 END nc
FROM @study_results_schema.dashboard d
INNER JOIN @study_results_schema.cohort_definition t ON d.target_cohort_definition_id = t.cohort_definition_id
INNER JOIN @study_results_schema.cohort_definition o ON d.outcome_cohort_definition_id = o.cohort_definition_id
INNER JOIN @study_results_schema.concept c ON c.concept_id = d.outcome_concept_id
LEFT JOIN @study_results_schema.cohort_seriousness s ON s.cohort_definition_id = o.cohort_definition_id
LEFT JOIN @study_results_schema.product_label p ON p.target_cohort_definition_id = d.target_cohort_definition_id
AND p.outcome_cohort_definition_id = d.outcome_cohort_definition_id
LEFT JOIN (
    SELECT DISTINCT target_cohort_definition_id, nc_cohort_definition_id
    FROM @study_results_schema.negative_controls
) nc ON nc.target_cohort_definition_id = d.target_cohort_definition_id 
AND nc.nc_cohort_definition_id = d.outcome_cohort_definition_id
WHERE d.target_cohort_definition_id @cohort_list_equality
  AND d.study_id = @study_id
;
