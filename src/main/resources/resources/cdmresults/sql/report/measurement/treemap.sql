SELECT
  concept_hierarchy.concept_id                          AS "conceptId",
  isNull(concept_hierarchy.level3_concept_name, 'NA')
  + '||' + isNull(concept_hierarchy.level2_concept_name, 'NA')
  + '||' + isNull(concept_hierarchy.level1_concept_name, 'NA')
  + '||' + isNull(concept_hierarchy.concept_name, 'NA') AS "conceptPath",
  ar1.count_value                                       AS "numPersons",
  1.0 * ar1.count_value / denom.count_value             AS "percentPersons",
  1.0 * ar2.count_value / ar1.count_value               AS "recordsPerPerson"
FROM (SELECT *
      FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 1800) ar1
  INNER JOIN
  (SELECT *
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 1801) ar2
    ON ar1.stratum_1 = ar2.stratum_1
  INNER JOIN
  @results_database_schema.concept_hierarchy_measurement AS concept_hierarchy
ON CAST(ar1.stratum_1 AS INT) = concept_hierarchy.concept_id
  ,
  (SELECT count_value
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 1) denom
ORDER BY ar1.count_value DESC
