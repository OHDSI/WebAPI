SELECT
  concept_hierarchy.concept_id,
  isNull(concept_hierarchy.level3_concept_name, 'NA')
  + '||' + isNull(concept_hierarchy.level2_concept_name, 'NA')
  + '||' + isNull(concept_hierarchy.level1_concept_name, 'NA')
  + '||' + isNull(concept_hierarchy.concept_name, 'NA') AS concept_path,
  ar1.count_value                                       AS num_persons,
  1.0 * ar1.count_value / denom.count_value             AS percent_persons,
  1.0 * ar2.count_value / ar1.count_value               AS records_per_person
FROM (SELECT *
      FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 1800) ar1
  INNER JOIN
  (SELECT *
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 1801) ar2
    ON ar1.stratum_1 = ar2.stratum_1
  INNER JOIN
  (
    SELECT
      m.concept_id,
      m.concept_name,
      max(c1.concept_name) AS level1_concept_name,
      max(c2.concept_name) AS level2_concept_name,
      max(c3.concept_name) AS level3_concept_name
    FROM
      (
        SELECT DISTINCT
          concept_id,
          concept_name
        FROM @vocab_database_schema.concept c
        JOIN @cdm_database_schema.measurement M ON c.concept_id = M.measurement_concept_id
      ) m LEFT JOIN
      @vocab_database_schema.concept_ancestor ca1 ON M.concept_id = ca1.DESCENDANT_CONCEPT_ID AND ca1.min_levels_of_separation = 1
                                                                                                     LEFT JOIN @vocab_database_schema.concept c1 ON ca1.ANCESTOR_CONCEPT_ID = c1.concept_id
                                                                                                                                                 LEFT JOIN @vocab_database_schema.concept_ancestor ca2 ON c1.concept_id = ca2.DESCENDANT_CONCEPT_ID AND ca2.min_levels_of_separation = 1
                                                                                                                                                                                                                             LEFT JOIN @vocab_database_schema.concept c2 ON ca2.ANCESTOR_CONCEPT_ID = c2.concept_id
                                                                                                                                                                                                                                                                                                        LEFT JOIN @vocab_database_schema.concept_ancestor ca3 ON c2.concept_id = ca3.DESCENDANT_CONCEPT_ID AND ca3.min_levels_of_separation = 1
                                                                                                                                                                                                                                                                                                                                                                                                                                            LEFT JOIN @vocab_database_schema.concept c3 ON ca3.ANCESTOR_CONCEPT_ID = c3.concept_id
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              GROUP BY M.concept_id, M.concept_name
  ) concept_hierarchy ON ar1.stratum_1 = CAST(concept_hierarchy.concept_id AS VARCHAR)
  ,
  (SELECT count_value
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 1) denom
ORDER BY ar1.count_value DESC
