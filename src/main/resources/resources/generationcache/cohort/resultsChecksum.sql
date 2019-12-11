WITH partials AS (
  SELECT
    CONCAT(
      ISNULL(CAST(subject_id AS VARCHAR), ' '),
      CONCAT(
        ISNULL(CAST(YEAR(cohort_start_date) AS VARCHAR), ' '),
        ISNULL(CAST(MONTH(cohort_start_date) AS VARCHAR), ' '),
        ISNULL(CAST(DAY(cohort_start_date) AS VARCHAR), ' ')
      ),
      CONCAT(
        ISNULL(CAST(YEAR(cohort_end_date) AS VARCHAR), ' '),
        ISNULL(CAST(MONTH(cohort_end_date) AS VARCHAR), ' '),
        ISNULL(CAST(DAY(cohort_end_date) AS VARCHAR), ' ')
      )
    ) AS line,
    subject_id AS sort_by
  FROM @results_database_schema.cohort_cache
  WHERE design_hash = @design_hash

  UNION ALL

  SELECT
    CONCAT(
      ISNULL(CAST(mode_id AS VARCHAR), ' '),
      ISNULL(CAST(inclusion_rule_mask AS VARCHAR), ' '),
      ISNULL(CAST(person_count AS VARCHAR), ' ')
    ),
    mode_id AS sort_by
  FROM @results_database_schema.cohort_inclusion_result_cache
  WHERE design_hash = @design_hash

  UNION ALL

  SELECT
    CONCAT(
      ISNULL(CAST(rule_sequence AS VARCHAR), ' '),
      ISNULL(CAST(mode_id AS VARCHAR), ' '),
      ISNULL(CAST(person_count AS VARCHAR), ' '),
      ISNULL(CAST(gain_count AS VARCHAR), ' '),
      ISNULL(CAST(person_total AS VARCHAR), ' ')
    ),
    rule_sequence AS sort_by
  FROM @results_database_schema.cohort_inclusion_stats_cache
  WHERE design_hash = @design_hash

  UNION ALL

  SELECT
    CONCAT(
      ISNULL(CAST(mode_id AS VARCHAR), ' '),
      ISNULL(CAST(base_count AS VARCHAR), ' '),
      ISNULL(CAST(final_count AS VARCHAR), ' ')
    ),
    mode_id AS sort_by
  FROM @results_database_schema.cohort_summary_stats_cache
  WHERE design_hash = @design_hash

  UNION ALL

  SELECT ISNULL(CAST(lost_count AS VARCHAR), ' '), 0
  FROM @results_database_schema.cohort_censor_stats_cache
  WHERE design_hash = @design_hash
)
SELECT AVG(
  CAST(CAST(CONVERT(VARBINARY, HASHBYTES('MD5',line), 1) AS INT) AS BIGINT)
) as checksum
FROM partials;