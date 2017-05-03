SELECT
  concept_hierarchy.rxnorm_ingredient_concept_id                 AS "conceptId",
  isnull(concept_hierarchy.atc1_concept_name, 'NA') + '||' +
  isnull(concept_hierarchy.atc3_concept_name, 'NA') + '||' +
  isnull(concept_hierarchy.atc5_concept_name, 'NA') + '||' +
  isnull(concept_hierarchy.rxnorm_ingredient_concept_name, '||') AS "conceptPath",
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
  @results_database_schema.concept_hierarchy_drug_era AS concept_hierarchy
    ON ar1.stratum_1 = CAST(concept_hierarchy.rxnorm_ingredient_concept_id AS VARCHAR)
  ,
  (SELECT count_value
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 1) denom
