SELECT DISTINCT
	d.outcome_cohort_definition_id
	, o.cohort_definition_name outcome_cohort_name
	, d.outcome_concept_id
FROM @study_results_schema.dashboard d
INNER JOIN @study_results_schema.cohort_definition o ON d.outcome_cohort_definition_id = o.cohort_definition_id
WHERE d.target_cohort_definition_id @cohort_list_equality
  AND d.study_id = @study_id
ORDER BY o.cohort_definition_name
;
