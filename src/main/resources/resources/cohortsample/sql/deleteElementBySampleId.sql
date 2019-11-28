DELETE FROM @results_schema.cohort_sample_element se
WHERE se.cohort_sample_id = @cohortSampleId
;