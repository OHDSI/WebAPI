SELECT
  cohort_generations_record_count,
  cohort_inclusion_count,
  cohort_inclusion_result_count,
  cohort_inclusion_stats_count,
  cohort_summary_stats_count
FROM (
  SELECT COUNT(*) as cohort_generations_record_count
  FROM @results_database_schema.cohort_generations
  WHERE generation_id = @generation_id
) a,
(
  SELECT COUNT(*) as cohort_inclusion_count
  FROM @results_database_schema.cohort_inclusion
  WHERE generation_id = @generation_id
) b,
(
  SELECT COUNT(*) as cohort_inclusion_result_count
  FROM @results_database_schema.cohort_inclusion_result
  WHERE generation_id = @generation_id
) c,
(
  SELECT COUNT(*) as cohort_inclusion_stats_count
  FROM @results_database_schema.cohort_inclusion_stats
  WHERE generation_id = @generation_id
) d,
(
  SELECT COUNT(*) as cohort_summary_stats_count
  FROM @results_database_schema.cohort_summary_stats
  WHERE generation_id = @generation_id
) e;