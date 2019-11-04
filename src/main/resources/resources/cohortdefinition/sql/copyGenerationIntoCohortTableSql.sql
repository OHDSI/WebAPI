DELETE FROM @results_database_schema.cohort_generations_ref WHERE cohort_definition_id = @cohort_definition_id;
INSERT INTO @results_database_schema.cohort_generations_ref (generation_id, cohort_definition_id) VALUES (@generation_id, @cohort_definition_id);

DELETE FROM @results_database_schema.cohort WHERE cohort_definition_id = @cohort_definition_id;

INSERT INTO @results_database_schema.cohort (cohort_definition_id, subject_id, cohort_start_date, cohort_end_date)
SELECT @cohort_definition_id, subject_id, cohort_start_date, cohort_end_date
FROM @results_database_schema.cohort_generations cg
  JOIN @results_database_schema.cohort_generations_ref cgr ON cg.generation_id = cgr.generation_id
WHERE cgr.cohort_definition_id = @cohort_definition_id;