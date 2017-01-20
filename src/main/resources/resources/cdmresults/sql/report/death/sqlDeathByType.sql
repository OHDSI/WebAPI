SELECT
  c2.concept_id   AS "conceptId",
  c2.concept_name AS "conceptName",
  ar1.count_value AS "countValue"
FROM @results_database_schema.ACHILLES_results ar1
INNER JOIN @vocab_database_schema.concept c2 ON ar1.stratum_1 = CAST(c2.concept_id AS VARCHAR )
WHERE ar1.analysis_id = 505