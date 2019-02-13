SELECT
  c1.concept_id   AS drug_concept_id,
  c2.concept_id   AS concept_id,
  c2.concept_name AS concept_name,
  ar1.count_value AS count_value
FROM @results_database_schema.achilles_results ar1
INNER JOIN
@vocab_database_schema.concept c1
ON ar1.stratum_1 = CAST(c1.concept_id AS VARCHAR(255))
INNER JOIN
@vocab_database_schema.concept c2
ON ar1.stratum_2 = CAST(c2.concept_id AS VARCHAR(255))
WHERE ar1.analysis_id = 705
AND ar1.stratum_1 = CAST('@conceptId' AS VARCHAR(255))