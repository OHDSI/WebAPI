DELETE FROM @results_database_schema.cohort_generations_ref WHERE generation_id = @generation_id;
DELETE FROM @results_database_schema.cohort_generations WHERE generation_id = @generation_id;
DELETE FROM @results_database_schema.cohort_inclusion WHERE generation_id = @generation_id;
DELETE FROM @results_database_schema.cohort_inclusion_result WHERE generation_id = @generation_id;
DELETE FROM @results_database_schema.cohort_inclusion_stats WHERE generation_id = @generation_id;
DELETE FROM @results_database_schema.cohort_summary_stats WHERE generation_id = @generation_id;
DELETE FROM @results_database_schema.cohort_censor_stats WHERE generation_id = @generation_id;