SELECT
  concept_hierarchy.concept_id                        AS conceptId,
  CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(
    isNull(concept_hierarchy.soc_concept_name, 'NA'), '||'),
    isNull(concept_hierarchy.hlgt_concept_name, 'NA')), '||'),
    isNull(concept_hierarchy.hlt_concept_name, 'NA')), '||'),
    isNull(concept_hierarchy.pt_concept_name, 'NA')), '||'),
    isNull(concept_hierarchy.snomed_concept_name, 'NA')) AS conceptPath,
  ar1.count_value                                     AS numPersons,
  ROUND(1.0 * ar1.count_value / denom.count_value, 5) AS percentPersons,
  ROUND(ar2.avg_value, 5)                             AS lengthOfEra
FROM (SELECT *
      FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 1000) ar1
  INNER JOIN
  (SELECT
     stratum_1,
     avg_value
   FROM @results_database_schema.ACHILLES_results_dist WHERE analysis_id = 1007) ar2
    ON ar1.stratum_1 = ar2.stratum_1
  INNER JOIN
  (
    SELECT
      snomed.concept_id,
      snomed.concept_name AS snomed_concept_name,
      pt_to_hlt.pt_concept_name,
      hlt_to_hlgt.hlt_concept_name,
      hlgt_to_soc.hlgt_concept_name,
      soc.concept_name    AS soc_concept_name
    FROM
      (
        SELECT
          concept_id,
          concept_name
        FROM @vocab_database_schema.concept
        WHERE domain_id = 'Condition'
      ) snomed
      LEFT JOIN
      (SELECT
         c1.concept_id      AS snomed_concept_id,
         max(c2.concept_id) AS pt_concept_id
       FROM
         @vocab_database_schema.concept c1
      INNER JOIN
      @vocab_database_schema.concept_ancestor ca1
      ON c1.concept_id = ca1.descendant_concept_id
                       AND c1.domain_id = 'Condition'
      AND ca1.min_levels_of_separation = 1
      INNER JOIN
      @vocab_database_schema.concept c2
                             ON ca1.ancestor_concept_id = c2.concept_id
      AND c2.vocabulary_id = 'MedDRA'
      GROUP BY c1.concept_id
      ) snomed_to_pt
        ON snomed.concept_id = snomed_to_pt.snomed_concept_id

      LEFT JOIN
      (SELECT
         c1.concept_id      AS pt_concept_id,
         c1.concept_name    AS pt_concept_name,
         max(c2.concept_id) AS hlt_concept_id
       FROM
         @vocab_database_schema.concept c1
      INNER JOIN
      @vocab_database_schema.concept_ancestor ca1
      ON c1.concept_id = ca1.descendant_concept_id
                       AND c1.vocabulary_id = 'MedDRA'
      AND ca1.min_levels_of_separation = 1
      INNER JOIN
      @vocab_database_schema.concept c2
                             ON ca1.ancestor_concept_id = c2.concept_id
      AND c2.vocabulary_id = 'MedDRA'
      GROUP BY c1.concept_id, c1.concept_name
      ) pt_to_hlt
        ON snomed_to_pt.pt_concept_id = pt_to_hlt.pt_concept_id

      LEFT JOIN
      (SELECT
         c1.concept_id      AS hlt_concept_id,
         c1.concept_name    AS hlt_concept_name,
         max(c2.concept_id) AS hlgt_concept_id
       FROM
         @vocab_database_schema.concept c1
      INNER JOIN
      @vocab_database_schema.concept_ancestor ca1
      ON c1.concept_id = ca1.descendant_concept_id
                       AND c1.vocabulary_id = 'MedDRA'
      AND ca1.min_levels_of_separation = 1
      INNER JOIN
      @vocab_database_schema.concept c2
                             ON ca1.ancestor_concept_id = c2.concept_id
      AND c2.vocabulary_id = 'MedDRA'
      GROUP BY c1.concept_id, c1.concept_name
      ) hlt_to_hlgt
        ON pt_to_hlt.hlt_concept_id = hlt_to_hlgt.hlt_concept_id

      LEFT JOIN
      (SELECT
         c1.concept_id      AS hlgt_concept_id,
         c1.concept_name    AS hlgt_concept_name,
         max(c2.concept_id) AS soc_concept_id
       FROM
         @vocab_database_schema.concept c1
      INNER JOIN
      @vocab_database_schema.concept_ancestor ca1
      ON c1.concept_id = ca1.descendant_concept_id
                       AND c1.vocabulary_id = 'MedDRA'
      AND ca1.min_levels_of_separation = 1
      INNER JOIN
      @vocab_database_schema.concept c2
                             ON ca1.ancestor_concept_id = c2.concept_id
      AND c2.vocabulary_id = 'MedDRA'
      GROUP BY c1.concept_id, c1.concept_name
      ) hlgt_to_soc
        ON hlt_to_hlgt.hlgt_concept_id = hlgt_to_soc.hlgt_concept_id

      LEFT JOIN @vocab_database_schema.concept soc
    ON hlgt_to_soc.soc_concept_id = soc.concept_id


  ) concept_hierarchy
    ON ar1.stratum_1 = cAST(concept_hierarchy.concept_id AS VARCHAR)
  ,
  (SELECT count_value
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 1) denom
ORDER BY ar1.count_value DESC