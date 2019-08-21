INSERT INTO @results_database_schema.cohort (cohort_definition_id, subject_id, cohort_start_date, cohort_end_date)
SELECT @cohort_definition_id, subject_id, cohort_start_date, cohort_end_date
FROM (@cached_result_sql) generations;