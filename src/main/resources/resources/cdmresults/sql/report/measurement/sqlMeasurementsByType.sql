SELECT
  c1.concept_id   AS MEASUREMENT_CONCEPT_ID,
  c1.concept_name AS MEASUREMENT_CONCEPT_NAME,
  c2.concept_id   AS CONCEPT_ID,
  c2.concept_name AS CONCEPT_NAME,
  ar1.count_value AS COUNT_VALUE
FROM @results_database_schema.ACHILLES_results ar1
INNER JOIN @vocab_database_schema.concept c1 ON ar1.stratum_1 = CAST(c1.concept_id AS VARCHAR )
INNER JOIN @vocab_database_schema.concept c2 ON ar1.stratum_2 = CAST(c2.concept_id AS VARCHAR )
WHERE ar1.analysis_id = 1805