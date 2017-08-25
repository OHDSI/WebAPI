SELECT
  concept_hierarchy.concept_id                               AS conceptId,
  CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(
    isNull(concept_hierarchy.level4_concept_name, 'NA'), '||'),
    isNull(concept_hierarchy.level3_concept_name, 'NA')), '||'),
    isNull(concept_hierarchy.level2_concept_name, 'NA')), '||'),
    isNull(concept_hierarchy.proc_concept_name, 'NA'))       AS conceptPath,
  ar1.count_value                                            AS numPersons,
  1.0 * ar1.count_value / denom.count_value                  AS percentPersons,
  1.0 * ar2.count_value / ar1.count_value                    AS recordsPerPerson
FROM (SELECT *
      FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 600) ar1
  INNER JOIN
  (SELECT *
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 601) ar2
    ON ar1.stratum_1 = ar2.stratum_1
  INNER JOIN
  (
    SELECT
      procs.concept_id,
      procs.proc_concept_name,
      max(proc_hierarchy.os3_concept_name) AS level2_concept_name,
      max(proc_hierarchy.os2_concept_name) AS level3_concept_name,
      max(proc_hierarchy.os1_concept_name) AS level4_concept_name
    FROM
      (
        SELECT
          c1.concept_id,
          CONCAT(CONCAT(CONCAT(CONCAT(v1.vocabulary_name, ' '), c1.concept_code), ': '), c1.concept_name) AS proc_concept_name
        FROM @vocab_database_schema.concept c1
        INNER JOIN @vocab_database_schema.vocabulary v1
        ON c1.vocabulary_id = v1.vocabulary_id
                            WHERE c1.domain_id = 'Procedure'
      ) procs
      LEFT JOIN
      (SELECT
         ca0.DESCENDANT_CONCEPT_ID,
         max(ca0.ancestor_concept_id) AS ancestor_concept_id
       FROM @vocab_database_schema.concept_ancestor ca0
      INNER JOIN
      ( SELECT DISTINCT c2.concept_id AS os3_concept_id
        FROM @vocab_database_schema.concept_ancestor ca1
                                    INNER JOIN
                                    @vocab_database_schema.concept c1
                                    ON ca1.DESCENDANT_CONCEPT_ID = c1.concept_id
                                          INNER JOIN
                                          @vocab_database_schema.concept_ancestor ca2
                                                                 ON c1.concept_id = ca2.ANCESTOR_CONCEPT_ID
      INNER JOIN
      @vocab_database_schema.concept c2
      ON ca2.DESCENDANT_CONCEPT_ID = c2.concept_id
                                   WHERE ca1.ancestor_concept_id = 4040390
      AND ca1.Min_LEVELS_OF_SEPARATION = 2
      AND ca2.MIN_LEVELS_OF_SEPARATION = 1
      ) t1

        ON ca0.ANCESTOR_CONCEPT_ID = t1.os3_concept_id

        GROUP BY ca0.descendant_concept_id

      ) ca1
        ON procs.concept_id = ca1.DESCENDANT_CONCEPT_ID
      LEFT JOIN
      (
        SELECT
          proc_by_os1.os1_concept_name,
          proc_by_os2.os2_concept_name,
          proc_by_os3.os3_concept_name,
          proc_by_os3.os3_concept_id
        FROM
          (SELECT
             DESCENDANT_CONCEPT_ID AS os1_concept_id,
             concept_name          AS os1_concept_name
           FROM @vocab_database_schema.concept_ancestor ca1
          INNER JOIN
          @vocab_database_schema.concept c1
          ON ca1.DESCENDANT_CONCEPT_ID = c1.concept_id
                                       WHERE ancestor_concept_id = 4040390
                                       AND Min_LEVELS_OF_SEPARATION = 1
          ) proc_by_os1

          INNER JOIN
          (SELECT
             max(c1.CONCEPT_ID) AS os1_concept_id,
             c2.concept_id      AS os2_concept_id,
             c2.concept_name    AS os2_concept_name
           FROM @vocab_database_schema.concept_ancestor ca1
          INNER JOIN
          @vocab_database_schema.concept c1
          ON ca1.DESCENDANT_CONCEPT_ID = c1.concept_id
                                       INNER JOIN
                                       @vocab_database_schema.concept_ancestor ca2
          ON c1.concept_id = ca2.ANCESTOR_CONCEPT_ID
          INNER JOIN
          @vocab_database_schema.concept c2
          ON ca2.DESCENDANT_CONCEPT_ID = c2.concept_id
                                           WHERE ca1.ancestor_concept_id = 4040390
                                           AND ca1.Min_LEVELS_OF_SEPARATION = 1
                                                  AND ca2.MIN_LEVELS_OF_SEPARATION = 1
           GROUP BY c2.concept_id, c2.concept_name
          ) proc_by_os2
            ON proc_by_os1.os1_concept_id = proc_by_os2.os1_concept_id

          INNER JOIN
          (SELECT
             max(c1.CONCEPT_ID) AS os2_concept_id,
             c2.concept_id      AS os3_concept_id,
             c2.concept_name    AS os3_concept_name
           FROM @vocab_database_schema.concept_ancestor ca1
          INNER JOIN
          @vocab_database_schema.concept c1
          ON ca1.DESCENDANT_CONCEPT_ID = c1.concept_id
                                       INNER JOIN
                                       @vocab_database_schema.concept_ancestor ca2
          ON c1.concept_id = ca2.ANCESTOR_CONCEPT_ID
          INNER JOIN
          @vocab_database_schema.concept c2
          ON ca2.DESCENDANT_CONCEPT_ID = c2.concept_id
                                           WHERE ca1.ancestor_concept_id = 4040390
                                           AND ca1.Min_LEVELS_OF_SEPARATION = 2
                                                  AND ca2.MIN_LEVELS_OF_SEPARATION = 1
           GROUP BY c2.concept_id, c2.concept_name
          ) proc_by_os3
            ON proc_by_os2.os2_concept_id = proc_by_os3.os2_concept_id
      ) proc_hierarchy
        ON ca1.ancestor_concept_id = proc_hierarchy.os3_concept_id
    GROUP BY procs.concept_id,
      procs.proc_concept_name

  ) concept_hierarchy
    ON ar1.stratum_1 = CAST(concept_hierarchy.concept_id AS VARCHAR)
  ,
  (SELECT count_value
   FROM @results_database_schema.ACHILLES_results WHERE analysis_id = 1) denom

