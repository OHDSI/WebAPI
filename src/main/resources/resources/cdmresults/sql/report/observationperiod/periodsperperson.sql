SELECT
  row_number()
  OVER (
    ORDER BY ar1.stratum_1) AS concept_id,
  ar1.stratum_1             AS concept_name,
  ar1.count_value           AS count_value
FROM @results_database_schema.ACHILLES_results ar1
WHERE ar1.analysis_id = 113