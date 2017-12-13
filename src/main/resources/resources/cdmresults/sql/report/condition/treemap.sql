SELECT
  concept_hierarchy.concept_id,
  isNull(concept_hierarchy.level4_concept_name, 'NA') + '||' + isNull(concept_hierarchy.level3_concept_name, 'NA') + '||' +
  isNull(concept_hierarchy.level2_concept_name, 'NA') + '||' + isNull(concept_hierarchy.level2_concept_name, 'NA') + '||' +
  isNull(concept_hierarchy.concept_name, 'NA') AS concept_path,
  ar1.count_value                                     AS num_persons,
  round(1.0 * ar1.count_value / denom.count_value, 5) AS percent_persons,
  round(1.0 * ar2.count_value / ar1.count_value, 5)   AS records_per_person
FROM (SELECT *
      FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 400) ar1
  INNER JOIN
  (SELECT *
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 401) ar2
    ON ar1.stratum_1 = ar2.stratum_1
  INNER JOIN
  @results_database_schema.concept_hierarchy concept_hierarchy
    ON CAST(ar1.stratum_1 AS INT) = concept_hierarchy.concept_id
    AND concept_hierarchy.treemap='Condition'
,
  (SELECT count_value
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 1) denom

ORDER BY ar1.count_value DESC
