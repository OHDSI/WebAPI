SELECT
  c2.concept_id   AS concept_id,
  c2.concept_name AS concept_name,
  ar1.count_value AS count_value
FROM @results_database_schema.ACHILLES_results ar1
INNER JOIN @vocab_database_schema.concept c2 ON ar1.stratum_1 = CAST(c2.concept_id AS VARCHAR )
WHERE ar1.analysis_id = 505