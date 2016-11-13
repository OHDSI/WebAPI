SELECT
  row_number()
  OVER (
    ORDER BY ar1.stratum_1) AS conceptId,
  ar1.stratum_1             AS conceptName,
  ar1.count_value           AS countValue
FROM @results_database_schema.ACHILLES_results ar1
WHERE ar1.analysis_id = 113