SELECT
  c1.concept_id                          AS "measurementConceptId",
  c1.concept_name                        AS "measurementConceptName",
  c2.concept_id                          AS "conceptId",
  c2.concept_name + ': ' + ar1.stratum_3 AS "conceptName",
  ar1.count_value                        AS "countValue"
FROM @results_database_schema.ACHILLES_results ar1
INNER JOIN @vocab_database_schema.concept c1 ON CAST(ar1.stratum_1 AS INT) = c1.concept_id
INNER JOIN @vocab_database_schema.concept c2 ON CAST(ar1.stratum_2 AS INT) = c2.concept_id
WHERE ar1.analysis_id = 1818