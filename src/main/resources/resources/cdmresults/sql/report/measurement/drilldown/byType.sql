SELECT
  c1.concept_id   AS measurement_concept_id,
  c1.concept_name AS measurement_concept_name,
  c2.concept_id   AS concept_id,
  c2.concept_name AS concept_name,
  ar1.count_value AS count_value
FROM @results_database_schema.ACHILLES_results ar1
INNER JOIN @vocab_database_schema.concept c1 ON CAST(ar1.stratum_1 AS INT) = c1.concept_id
INNER JOIN @vocab_database_schema.concept c2 ON CAST(ar1.stratum_2 AS INT) = c2.concept_id
WHERE ar1.analysis_id = 1805
AND CAST(ar1.stratum_1 AS INT) = @conceptId