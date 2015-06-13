select distinct analysis_id
FROM  @tableQualifier.heracles_results 
WHERE COHORT_DEFINITION_ID = @id 
union
SELECT distinct analysis_id
FROM  @tableQualifier.heracles_results_dist 
WHERE COHORT_DEFINITION_ID = @id