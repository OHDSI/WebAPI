SELECT
  concept_hierarchy.concept_id                        AS "conceptId",
  isNull(concept_hierarchy.soc_concept_name, 'NA') + '||' + isNull(concept_hierarchy.hlgt_concept_name, 'NA') + '||' +
  isNull(concept_hierarchy.hlt_concept_name, 'NA') + '||' + isNull(concept_hierarchy.pt_concept_name, 'NA') + '||' +
  isNull(concept_hierarchy.snomed_concept_name, 'NA') AS "conceptPath",
  ar1.count_value                                     AS "numPersons",
  ROUND(1.0 * ar1.count_value / denom.count_value, 5) AS "percentPersons",
  ROUND(ar2.avg_value, 5)                             AS "lengthOfEra"
FROM (SELECT *
      FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 1000) ar1
  INNER JOIN
  (SELECT
     stratum_1,
     avg_value
   FROM @results_database_schema.ACHILLES_results_dist WHERE analysis_id = 1007) ar2
    ON ar1.stratum_1 = ar2.stratum_1
  INNER JOIN
  @results_database_schema.concept_hierarchy_condition AS concept_hierarchy
    ON CAST(ar1.stratum_1 AS INT) = concept_hierarchy.concept_id
  ,
  (SELECT count_value
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 1) denom
ORDER BY ar1.count_value DESC