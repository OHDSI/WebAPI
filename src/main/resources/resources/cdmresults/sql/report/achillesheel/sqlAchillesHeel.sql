SELECT
  analysis_id           AS attribute_name,
  achilles_heel_warning AS attribute_value
FROM @results_database_schema.achilles_heel_results
ORDER BY CASE WHEN LEFT(achilles_heel_warning, 5) = 'Error' THEN 1 ELSE 2 END , analysis_id
