DELETE FROM @results_schema.cohort_sample_element
WHERE cohort_sample_id IN ( @cohortSampleId )
;