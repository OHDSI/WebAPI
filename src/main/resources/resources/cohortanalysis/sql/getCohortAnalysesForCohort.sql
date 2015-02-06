SELECT DISTINCT a.*,
                @cohortDefinitionId AS COHORT_DEFINITION_ID,
                CASE
                    WHEN r.COHORT_DEFINITION_ID IS NULL THEN 0
                    ELSE 1
                END AS ANALYSIS_COMPLETE
FROM @resultsSchema.HERACLES_ANALYSIS a
LEFT OUTER JOIN @resultsSchema.@heraclesResultsTable r ON r.ANALYSIS_ID = a.ANALYSIS_ID
WHERE (r.COHORT_DEFINITION_ID = @cohortDefinitionId
       OR r.COHORT_DEFINITION_ID IS NULL)
ORDER BY a.ANALYSIS_ID