SELECT AVG(CAST(CAST(('x' || hash) AS bit(32)) AS bigint)) as checksum
FROM (
  select md5(
    coalesce(CAST(subject_id AS VARCHAR), ' ') ||
    coalesce(CAST(EXTRACT(EPOCH FROM cohort_start_date) AS VARCHAR), ' ') ||
    coalesce(CAST(EXTRACT(EPOCH FROM cohort_end_date) AS VARCHAR), ' ')
  ) as hash
  from @results_database_schema.cohort_generations
  where cohort_definition_id = @cohort_definition_id
) list;