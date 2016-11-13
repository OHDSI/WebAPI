SELECT
  c1.concept_id                             AS conceptId,
  c1.concept_name                           AS conceptPath,
  ar1.count_value                           AS numPersons,
  1.0 * ar1.count_value / denom.count_value AS percentPersons,
  1.0 * ar2.count_value / ar1.count_value   AS recordsPerPerson
FROM (SELECT *
      FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 200) ar1
  INNER JOIN
  (SELECT *
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 201) ar2 ON ar1.stratum_1 = ar2.stratum_1
  INNER JOIN @vocab_database_schema.concept c1 ON ar1.stratum_1 = CAST(c1.concept_id AS VARCHAR ),
( SELECT count_value FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 1) denom

