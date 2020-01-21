DELETE FROM @results_schema.cohort_sample_element s
WHERE s.cohort_sample_id IN ( @cohortSampleId )
;