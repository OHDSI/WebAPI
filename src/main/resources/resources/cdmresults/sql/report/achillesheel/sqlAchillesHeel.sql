SELECT
  analysis_id           AS "attributename",
  ACHILLES_HEEL_warning AS "attributevalue"
FROM @results_database_schema.ACHILLES_HEEL_results
ORDER BY CASE WHEN LEFT(ACHILLES_HEEL_warning, 5) = 'Error' THEN 1 ELSE 2 END , analysis_id