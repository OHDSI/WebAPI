SELECT
  c1.concept_id                             AS concept_id,
  c1.concept_name                           AS concept_path,
  ar1.count_value                           AS num_persons,
  1.0 * ar1.count_value / denom.count_value AS percent_persons,
  1.0 * ar2.count_value / ar1.count_value   AS records_per_person
FROM (SELECT *
      FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 200) ar1
  INNER JOIN
  (SELECT *
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 201) ar2 ON ar1.stratum_1 = ar2.stratum_1
  INNER JOIN @vocab_database_schema.concept c1
ON CAST(ar1.stratum_1 AS INT) = c1.concept_id,
( SELECT count_value FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 1) denom

