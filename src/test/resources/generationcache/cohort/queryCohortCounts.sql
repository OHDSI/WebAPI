SELECT
  cohort_record_count,
  cohort_inclusion_result_count,
  cohort_inclusion_stats_count,
  cohort_summary_stats_count,
  cohort_censor_stats_count
FROM (
  SELECT COUNT(*) as cohort_record_count
  FROM @results_database_schema.cohort_cache
  WHERE design_hash = @design_hash
) a,
(
  SELECT COUNT(*) as cohort_inclusion_result_count
  FROM @results_database_schema.cohort_inclusion_result_cache
  WHERE design_hash = @design_hash
) b,
(
  SELECT COUNT(*) as cohort_inclusion_stats_count
  FROM @results_database_schema.cohort_inclusion_stats_cache
  WHERE design_hash = @design_hash
) c,
(
  SELECT COUNT(*) as cohort_summary_stats_count
  FROM @results_database_schema.cohort_summary_stats_cache
  WHERE design_hash = @design_hash
) d,
(
  SELECT COUNT(*) as cohort_censor_stats_count
  FROM @results_database_schema.cohort_censor_stats_cache
  WHERE design_hash = @design_hash
) e
;