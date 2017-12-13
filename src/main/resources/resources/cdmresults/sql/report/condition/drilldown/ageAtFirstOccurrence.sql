SELECT
  c1.concept_id,
  c2.concept_name   AS category,
  ard1.min_value,
  ard1.p10_value,
  ard1.p25_value,
  ard1.median_value,
  ard1.p75_value,
  ard1.p90_value,
  ard1.max_value
FROM @results_database_schema.ACHILLES_results_dist ard1
INNER JOIN
@vocab_database_schema.concept c1
ON CAST(ard1.stratum_1 AS INT) = c1.concept_id
INNER JOIN
@vocab_database_schema.concept c2
ON CAST(ard1.stratum_2 AS INT) = c2.concept_id
WHERE ard1.analysis_id = 406
AND c1.concept_id = @conceptId
