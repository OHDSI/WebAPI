SELECT *
FROM @results_schema.cohort_sample s
WHERE s.cohort_definition_id = @cohortDefinitionId
;