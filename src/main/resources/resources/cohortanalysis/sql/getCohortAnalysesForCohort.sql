SELECT DISTINCT a.*,
                @cohortDefinitionId AS COHORT_DEFINITION_ID,
                CASE
                    WHEN r.COHORT_DEFINITION_ID IS NULL THEN 0
                    ELSE 1
                END AS ANALYSIS_COMPLETE,
                r.LAST_UPDATE_TIME
FROM @resultsSchema.HERACLES_ANALYSIS a
LEFT OUTER JOIN 
-- We filter by ids here too just so it runs faster
  (SELECT cohort_definition_id,
          analysis_id,
          last_update_time
   FROM @resultsSchema.@heraclesResultsTable
   WHERE COHORT_DEFINITION_ID = @cohortDefinitionId
   UNION ALL SELECT cohort_definition_id,
                    analysis_id,
                    last_update_time
   FROM @resultsSchema.@heraclesResultsDistTable
   WHERE COHORT_DEFINITION_ID = @cohortDefinitionId) r ON r.ANALYSIS_ID = a.ANALYSIS_ID
WHERE (r.COHORT_DEFINITION_ID = @cohortDefinitionId
       OR r.COHORT_DEFINITION_ID IS NULL)
ORDER BY a.ANALYSIS_TYPE, a.ANALYSIS_NAME