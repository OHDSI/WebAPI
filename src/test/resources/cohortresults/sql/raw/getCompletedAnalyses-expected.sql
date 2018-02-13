select distinct analysis_id
FROM  result_schema.heracles_results 
WHERE COHORT_DEFINITION_ID = ? 
union
SELECT distinct analysis_id
FROM  result_schema.heracles_results_dist 
WHERE COHORT_DEFINITION_ID = ?