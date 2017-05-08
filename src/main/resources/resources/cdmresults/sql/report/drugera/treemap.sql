SELECT
  concept_hierarchy.concept_id                 AS "conceptId",
  isnull(concept_hierarchy.level3_concept_name, 'NA') + '||' +
  isnull(concept_hierarchy.level2_concept_name, 'NA') + '||' +
  isnull(concept_hierarchy.level1_concept_name, 'NA') + '||' +
  isnull(concept_hierarchy.concept_name, '||') AS "conceptPath",
  ar1.count_value                                                AS "numPersons",
  1.0 * ar1.count_value / denom.count_value                      AS "percentPersons",
  ar2.avg_value                                                  AS "lengthOfEra"
FROM (SELECT *
      FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 900) ar1
  INNER JOIN
  (SELECT
     stratum_1,
     avg_value
   FROM @results_database_schema.ACHILLES_results_dist WHERE analysis_id = 907) ar2
    ON ar1.stratum_1 = ar2.stratum_1
  INNER JOIN
  @results_database_schema.concept_hierarchy AS concept_hierarchy
    ON CAST(ar1.stratum_1 AS INT) = concept_hierarchy.concept_id
  AND concept_hierarchy.treemap='Drug Era'
  ,
  (SELECT count_value
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 1) denom
