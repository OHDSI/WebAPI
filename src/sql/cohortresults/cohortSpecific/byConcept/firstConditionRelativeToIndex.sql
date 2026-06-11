SELECT hr1.cohort_definition_id, 
       'First' AS record_type, 
       c1.concept_id, 
       c1.concept_name, 
       hr1.duration, 
       hr1.count_value, 
       CASE 
         WHEN t1.count_value > 0 THEN 1.0 * hr1.count_value / t1.count_value 
         ELSE 0 
       END     AS pct_persons 
FROM   (SELECT cohort_definition_id, 
               CAST(CASE WHEN isNumeric(stratum_1) = 1 THEN stratum_1 ELSE null END AS INT)      AS concept_id,
               CAST(CASE WHEN isNumeric(stratum_2) = 1 THEN stratum_2 ELSE null END AS INT) * 30 AS duration,
               count_value 
        FROM   @ohdsi_database_schema.heracles_results 
        WHERE  analysis_id IN ( 1820 ) 
               AND cohort_definition_id = @cohortDefinitionId
               AND CAST(CASE WHEN isNumeric(stratum_2) = 1 THEN stratum_2 ELSE null END AS INT) * 30 BETWEEN -1000 AND 1000
                        ) hr1 
       INNER JOIN (SELECT -1 * CAST(stratum_1 AS INT) * 30                AS
              duration, 
                          Sum(count_value) 
                            OVER ( 
                              ORDER BY -1* CAST(stratum_1 AS INT)*30 ASC) AS
              count_value 
                   FROM  (
                                             select CAST(stratum_1 AS VARCHAR(11)) AS stratum_1, max(count_value) as count_value
                                                  from
                                                  (
                                                  select row_number() over (order by analysis_id) as stratum_1, 0 as count_value
                                                  from @ohdsi_database_schema.heracles_results
                                                  where analysis_id = 1805
                                                  and cohort_definition_id = @cohortDefinitionId

                                                  union

                                                  select CAST(CASE WHEN isNumeric(stratum_1) = 1 THEN stratum_1 ELSE null END AS INT) as stratum_1, count_value
                                                  from @ohdsi_database_schema.heracles_results
                                                  where analysis_id = 1805
                                                  and cohort_definition_id = @cohortDefinitionId
                                                  ) t1
                                                  where stratum_1<=10 or count_value > 0
                                                  group by stratum_1
                                             ) t0
                                             
                                             
                                             WHERE CAST(stratum_1 AS INT) > 0
                   UNION 
                   SELECT CAST(CASE WHEN isNumeric(hr1.stratum_1) = 1 THEN hr1.stratum_1 ELSE null END AS INT) * 30
                          AS 
                          duration, 
                          t1.count_value - Sum(hr1.count_value) 
                          OVER ( 
                            partition BY hr1.cohort_definition_id 
                            ORDER BY CAST(CASE WHEN isNumeric(hr1.stratum_1) = 1 THEN hr1.stratum_1 ELSE null END AS INT)*
                          30 ASC) AS 
                          count_value 
                   FROM   @ohdsi_database_schema.heracles_results hr1 
                          INNER JOIN (SELECT cohort_definition_id, 
                                             Sum(count_value) AS count_value 
                                      FROM 
                          @ohdsi_database_schema.heracles_results 
                                      WHERE  analysis_id = 1806 
                                             AND cohort_definition_id = @cohortDefinitionId 
                                      GROUP  BY cohort_definition_id) t1 
                                  ON hr1.cohort_definition_id = 
                                     t1.cohort_definition_id 
                   WHERE  hr1.analysis_id IN ( 1806 ) 
                          AND hr1.cohort_definition_id = @cohortDefinitionId
                                                    ) t1 
               ON  hr1.duration = t1.duration 
       INNER JOIN (SELECT CAST(CASE WHEN isNumeric(stratum_1) = 1 THEN stratum_1 ELSE null END AS INT) AS concept_id,
                          Sum(count_value)           AS count_value 
                   FROM   @ohdsi_database_schema.heracles_results 
                   WHERE  analysis_id IN ( 1820 ) 
                           GROUP  BY CAST(CASE WHEN isNumeric(stratum_1) = 1 THEN stratum_1 ELSE null END AS INT)
										HAVING Sum(heracles_results.count_value) > @minCovariatePersonCount
                   ) ct1 
               ON hr1.concept_id = ct1.concept_id
       INNER JOIN @cdm_database_schema.concept c1 
               ON hr1.concept_id = c1.concept_id 
WHERE  c1.concept_id = @conceptId and t1.count_value > @minIntervalPersonCount 
UNION 
SELECT hr1.cohort_definition_id, 
       'All' AS record_type, 
       c1.concept_id, 
       c1.concept_name, 
       hr1.duration, 
       hr1.count_value, 
       CASE 
         WHEN t1.count_value > 0 THEN 1.0 * hr1.count_value / t1.count_value 
         ELSE 0 
       END   AS pct_persons 
FROM   (SELECT cohort_definition_id, 
               CAST(CASE WHEN isNumeric(stratum_1) = 1 THEN stratum_1 ELSE null END AS INT)      AS concept_id,
               CAST(CASE WHEN isNumeric(stratum_2) = 1 THEN stratum_2 ELSE null END AS INT) * 30 AS duration,
               count_value 
        FROM   @ohdsi_database_schema.heracles_results  
        WHERE  analysis_id IN ( 1821 ) 
               AND cohort_definition_id = @cohortDefinitionId
               AND CAST(CASE WHEN isNumeric(stratum_2) = 1 THEN stratum_2 ELSE null END AS INT) * 30 BETWEEN -1000 AND 1000
               ) hr1 
       INNER JOIN (SELECT -1 * CAST(stratum_1 AS INT) * 30                AS
              duration, 
                          Sum(count_value) 
                            OVER ( 
                              ORDER BY -1* CAST(stratum_1 AS INT)*30 ASC) AS
              count_value 
                   FROM  (
                                             select CAST(stratum_1 AS VARCHAR(11)) AS stratum_1, max(count_value) as count_value
                                                  from
                                                  (
                                                  select row_number() over (order by analysis_id) as stratum_1, 0 as count_value
                                                  from @ohdsi_database_schema.heracles_results
                                                  where analysis_id = 1805
                                                  and cohort_definition_id = @cohortDefinitionId

                                                  union

                                                  select CAST(CASE WHEN isNumeric(stratum_1) = 1 THEN stratum_1 ELSE null END AS INT) as stratum_1, count_value
                                                  from @ohdsi_database_schema.heracles_results
                                                  where analysis_id = 1805
                                                  and cohort_definition_id = @cohortDefinitionId
                                                  ) t1
                                                  where stratum_1<=10 or count_value > 0
                                                  group by stratum_1
                                             ) t0
                                             
                                             
                                             WHERE CAST(stratum_1 AS INT) > 0
                   UNION 
                   SELECT CAST(CASE WHEN isNumeric(hr1.stratum_1) = 1 THEN hr1.stratum_1 ELSE null END AS INT) * 30
                          AS 
                          duration, 
                          t1.count_value - Sum(hr1.count_value) 
                          OVER ( 
                            partition BY hr1.cohort_definition_id 
                            ORDER BY CAST(CASE WHEN isNumeric(hr1.stratum_1) = 1 THEN hr1.stratum_1 ELSE null END AS INT)*
                          30 ASC) AS 
                          count_value 
                   FROM   @ohdsi_database_schema.heracles_results hr1 
                          INNER JOIN (SELECT cohort_definition_id, 
                                             Sum(count_value) AS count_value 
                                      FROM 
                          @ohdsi_database_schema.heracles_results 
                                      WHERE  analysis_id = 1806 
                                             AND cohort_definition_id = @cohortDefinitionId 
                                      GROUP  BY cohort_definition_id) t1 
                                  ON hr1.cohort_definition_id = 
                                     t1.cohort_definition_id 
                   WHERE  hr1.analysis_id IN ( 1806 ) 
                          AND hr1.cohort_definition_id IN 
                              ( @cohortDefinitionId )
                                                    ) t1 
               ON  hr1.duration = t1.duration 
       INNER JOIN (SELECT CAST(CASE WHEN isNumeric(stratum_1) = 1 THEN stratum_1 ELSE null END AS INT) AS concept_id,
                          Sum(count_value)           AS count_value 
                   FROM   @ohdsi_database_schema.heracles_results 
                   WHERE  analysis_id IN ( 1820 ) 
                           GROUP  BY CAST(CASE WHEN isNumeric(stratum_1) = 1 THEN stratum_1 ELSE null END AS INT)
										HAVING Sum(heracles_results.count_value) > @minCovariatePersonCount
                   ) ct1 
               ON hr1.concept_id = ct1.concept_id 
       INNER JOIN @cdm_database_schema.concept c1 
               ON hr1.concept_id = c1.concept_id 
WHERE  c1.concept_id = @conceptId and t1.count_value > @minIntervalPersonCount 
