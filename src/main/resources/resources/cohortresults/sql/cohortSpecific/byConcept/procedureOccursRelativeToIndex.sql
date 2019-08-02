/*  
PROCEDURE_OCURRENCE  

--drilldown of when procedure occurs relative to index  
--graph:  scatterplot  
--analysis_id: 1830  
--x:  time (30-day increments)  
--y:  %     
*/ 
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
        WHERE  analysis_id IN ( 1830 ) 
               AND cohort_definition_id = @cohortDefinitionId) hr1 
       INNER JOIN (SELECT cohort_definition_id, 
                          -1 * CAST(CASE WHEN isNumeric(stratum_1) = 1 THEN stratum_1 ELSE null END AS INT) * 30                AS
              duration, 
                          Sum(count_value) 
                            OVER ( 
                              partition BY cohort_definition_id 
                              ORDER BY -1* CAST(CASE WHEN isNumeric(stratum_1) = 1 THEN stratum_1 ELSE null END AS INT)*30 ASC) AS
              count_value 
                   FROM   @ohdsi_database_schema.heracles_results 
                   WHERE  analysis_id IN ( 1805 ) 
                          AND cohort_definition_id = @cohortDefinitionId 
                          AND CAST(CASE WHEN isNumeric(stratum_1) = 1 THEN stratum_1 ELSE null END AS INT) > 0
                   UNION 
                   SELECT hr1.cohort_definition_id, 
                          CAST(CASE WHEN isNumeric(hr1.stratum_1) = 1 THEN hr1.stratum_1 ELSE null END AS INT) * 30
                          AS 
                          duration, 
                          t1.count_value - Sum(hr1.count_value) 
                          OVER ( 
                            partition BY hr1.cohort_definition_id 
                            ORDER BY  CAST(CASE WHEN isNumeric(hr1.stratum_1) = 1 THEN hr1.stratum_1 ELSE null END AS INT)*
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
                              ( @cohortDefinitionId )) t1 
               ON hr1.cohort_definition_id = t1.cohort_definition_id 
                  AND hr1.duration = t1.duration
          INNER JOIN (SELECT iq1.cohort_definition_id, iq1.concept_id, Sum(iq1.count_value)
                   FROM   (
                     SELECT cohort_definition_id,
                            CAST(CASE WHEN isNumeric(stratum_1) = 1 THEN stratum_1 ELSE null END AS INT) AS concept_id,
                            count_value
                     FROM   @ohdsi_database_schema.heracles_results
                     WHERE  analysis_id IN ( 1830 )
                            AND cohort_definition_id = @cohortDefinitionId
                   ) iq1
                   GROUP  BY iq1.cohort_definition_id, iq1.concept_id
                   HAVING Sum(iq1.count_value) > @minCovariatePersonCount) ct1
               ON hr1.cohort_definition_id = ct1.cohort_definition_id 
                  AND hr1.concept_id = ct1.concept_id 
       INNER JOIN @cdm_database_schema.concept c1 
               ON hr1.concept_id = c1.concept_id 
WHERE  t1.count_value > @minIntervalPersonCount 
       AND c1.concept_id = @conceptId 
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
        WHERE  analysis_id IN ( 1831 ) 
               AND cohort_definition_id = @cohortDefinitionId) hr1 
       INNER JOIN (SELECT cohort_definition_id, 
                          -1 * CAST(CASE WHEN isNumeric(stratum_1) = 1 THEN stratum_1 ELSE null END AS INT) * 30                AS
              duration, 
                          Sum(count_value) 
                            OVER ( 
                              partition BY cohort_definition_id 
                              ORDER BY -1* CAST(CASE WHEN isNumeric(stratum_1) = 1 THEN stratum_1 ELSE null END AS INT)*30 ASC) AS
              count_value 
                   FROM   @ohdsi_database_schema.heracles_results 
                   WHERE  analysis_id IN ( 1805 ) 
                          AND cohort_definition_id = @cohortDefinitionId 
                          AND CAST(CASE WHEN isNumeric(stratum_1) = 1 THEN stratum_1 ELSE null END AS INT) > 0
                   UNION 
                   SELECT hr1.cohort_definition_id, 
                          CAST(CASE WHEN isNumeric(hr1.stratum_1) = 1 THEN hr1.stratum_1 ELSE null END AS INT) * 30
                          AS 
                          duration, 
                          t1.count_value - Sum(hr1.count_value) 
                          OVER ( 
                            partition BY hr1.cohort_definition_id 
                            ORDER BY  CAST(CASE WHEN isNumeric(hr1.stratum_1) = 1 THEN hr1.stratum_1 ELSE null END AS INT)*
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
                              ( @cohortDefinitionId )) t1 
               ON hr1.cohort_definition_id = t1.cohort_definition_id 
                  AND hr1.duration = t1.duration
          INNER JOIN (SELECT iq2.cohort_definition_id, iq2.concept_id, Sum(iq2.count_value)
                   FROM   (
                     SELECT cohort_definition_id,
                            CAST(CASE WHEN isNumeric(stratum_1) = 1 THEN stratum_1 ELSE null END AS INT) AS concept_id,
                            count_value
                     FROM   @ohdsi_database_schema.heracles_results
                     WHERE  analysis_id IN ( 1831 )
                            AND cohort_definition_id = @cohortDefinitionId
                   ) iq2
                   GROUP  BY iq2.cohort_definition_id, iq2.concept_id
                   HAVING Sum(iq2.count_value) > @minCovariatePersonCount) ct1
               ON hr1.cohort_definition_id = ct1.cohort_definition_id 
                  AND hr1.concept_id = ct1.concept_id 
       INNER JOIN @cdm_database_schema.concept c1 
               ON hr1.concept_id = c1.concept_id 
WHERE  t1.count_value > @minIntervalPersonCount 
       AND c1.concept_id = @conceptId 