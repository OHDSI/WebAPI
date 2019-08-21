-- No need to check cohort_inclusion, cohort_inclusion_result, cohort_inclusion_stats, cohort_summary_stats, cohort_censor_stats
-- since they can change only if design (hash) changed and their change will affect cohort result set anyway

-- TODO: no, we actually need to verify that tables were not emptied, otherwise no stats will be shown at UI

SELECT AVG(CAST(CAST(('x' || hash) AS bit(32)) AS bigint)) as checksum
FROM (
  select md5(
    coalesce(CAST(subject_id AS VARCHAR), ' ') ||
    coalesce(CAST(EXTRACT(EPOCH FROM cohort_start_date) AS VARCHAR), ' ') ||
    coalesce(CAST(EXTRACT(EPOCH FROM cohort_end_date) AS VARCHAR), ' ')
  ) as hash
  from @results_database_schema.cohort_generations
  where generation_id = @cohort_definition_id
) list;