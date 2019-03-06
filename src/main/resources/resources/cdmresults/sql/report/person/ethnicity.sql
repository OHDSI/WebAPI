SELECT
  c1.concept_id   AS conceptId,
  c1.concept_name AS conceptName,
  ar1.count_value AS countValue
FROM @results_database_schema.ACHILLES_results ar1
INNER JOIN
@vocab_database_schema.concept c1
ON CAST(CASE WHEN isNumeric(ar1.stratum_1) = 1 THEN ar1.stratum_1 ELSE null END AS INT) = c1.concept_id
WHERE ar1.analysis_id = 5
