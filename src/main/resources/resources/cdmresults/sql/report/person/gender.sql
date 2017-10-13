SELECT
  c1.concept_id   AS conceptId,
  c1.concept_name AS conceptName,
  ar1.count_value AS countValue
FROM @results_database_schema.ACHILLES_results ar1
INNER JOIN
@vocab_database_schema.concept c1
ON CAST(ar1.stratum_1 AS INT) = c1.concept_id
WHERE ar1.analysis_id = 2
AND c1.concept_id IN (8507, 8532)