SELECT
  concept_hierarchy.rxnorm_ingredient_concept_id                 concept_id,
  isnull(concept_hierarchy.atc1_concept_name, 'NA') + '||' +
  isnull(concept_hierarchy.atc3_concept_name, 'NA') + '||' +
  isnull(concept_hierarchy.atc5_concept_name, 'NA') + '||' +
  isnull(concept_hierarchy.rxnorm_ingredient_concept_name, '||') concept_path,
  hr1.count_value                           AS                   num_persons,
  1.0 * hr1.count_value / denom.count_value AS                   percent_persons,
  hr2.avg_value                             AS                   length_of_era
FROM (SELECT *
      FROM @ohdsi_database_schema.achilles_results WHERE analysis_id = 900) hr1
  INNER JOIN
  (SELECT
     stratum_1,
     avg_value
   FROM @ohdsi_database_schema.achilles_results_dist WHERE analysis_id = 907) hr2
    ON hr1.stratum_1 = hr2.stratum_1
  INNER JOIN
  (
    SELECT
      rxnorm.rxnorm_ingredient_concept_id,
      rxnorm.rxnorm_ingredient_concept_name,
      atc5_to_atc3.atc5_concept_name,
      atc3_to_atc1.atc3_concept_name,
      atc1.concept_name AS atc1_concept_name
    FROM
      (
        SELECT
          c2.concept_id   AS rxnorm_ingredient_concept_id,
          c2.concept_name AS RxNorm_ingredient_concept_name
        FROM
          @vocabulary_database_schema.concept c2
        WHERE
        c2.vocabulary_id = 'RxNorm'
        AND c2.concept_class_id = 'Ingredient'
                                AND c2.concept_id IN (@conceptList)
      ) rxnorm
      LEFT JOIN
      (SELECT
         c1.concept_id      AS rxnorm_ingredient_concept_id,
         max(c2.concept_id) AS atc5_concept_id
       FROM
         @vocabulary_database_schema.concept c1
      INNER JOIN
      @vocabulary_database_schema.concept_ancestor ca1
      ON c1.concept_id = ca1.descendant_concept_id
                       AND c1.vocabulary_id = 'RxNorm'
      AND c1.concept_class_id = 'Ingredient'
      INNER JOIN
      @vocabulary_database_schema.concept c2
                                  ON ca1.ancestor_concept_id = c2.concept_id
      AND c2.vocabulary_id = 'ATC'
      AND c2.concept_class_id = 'ATC 4th'
                              WHERE c1.concept_id IN (@conceptList)
       GROUP BY c1.concept_id
      ) rxnorm_to_atc5
        ON rxnorm.rxnorm_ingredient_concept_id = rxnorm_to_atc5.rxnorm_ingredient_concept_id

      LEFT JOIN
      (SELECT
         c1.concept_id      AS atc5_concept_id,
         c1.concept_name    AS atc5_concept_name,
         max(c2.concept_id) AS atc3_concept_id
       FROM
         @vocabulary_database_schema.concept c1
      INNER JOIN
      @vocabulary_database_schema.concept_ancestor ca1
      ON c1.concept_id = ca1.descendant_concept_id
                       AND c1.vocabulary_id = 'ATC'
      AND c1.concept_class_id = 'ATC 4th'
      INNER JOIN
      @vocabulary_database_schema.concept c2
                                  ON ca1.ancestor_concept_id = c2.concept_id
      AND c2.vocabulary_id = 'ATC'
      AND c2.concept_class_id = 'ATC 2nd'
                              GROUP BY c1.concept_id, c1.concept_name
      ) atc5_to_atc3
        ON rxnorm_to_atc5.atc5_concept_id = atc5_to_atc3.atc5_concept_id

      LEFT JOIN
      (SELECT
         c1.concept_id      AS atc3_concept_id,
         c1.concept_name    AS atc3_concept_name,
         max(c2.concept_id) AS atc1_concept_id
       FROM
         @vocabulary_database_schema.concept c1
      INNER JOIN
      @vocabulary_database_schema.concept_ancestor ca1
      ON c1.concept_id = ca1.descendant_concept_id
                       AND c1.vocabulary_id = 'ATC'
      AND c1.concept_class_id = 'ATC 2nd'
      INNER JOIN
      @vocabulary_database_schema.concept c2
                                  ON ca1.ancestor_concept_id = c2.concept_id
      AND c2.vocabulary_id = 'ATC'
      AND c2.concept_class_id = 'ATC 1st'
                              GROUP BY c1.concept_id, c1.concept_name
      ) atc3_to_atc1
        ON atc5_to_atc3.atc3_concept_id = atc3_to_atc1.atc3_concept_id

      LEFT JOIN @vocabulary_database_schema.concept atc1
    ON atc3_to_atc1.atc1_concept_id = atc1.concept_id
  ) concept_hierarchy
    ON hr1.stratum_1 = CAST(concept_hierarchy.rxnorm_ingredient_concept_id AS VARCHAR)
  ,
  (SELECT count_value
   FROM @ohdsi_database_schema.achilles_results WHERE analysis_id = 1) denom
ORDER BY hr1.count_value DESC
