(SELECT
   aa1.analysis_name AS "attributeName",
   ar1.stratum_1     AS "attributeValue"
 FROM @results_database_schema.ACHILLES_analysis aa1
INNER JOIN
@results_database_schema.ACHILLES_results ar1
ON aa1.analysis_id = ar1.analysis_id
                   WHERE aa1.analysis_id = 0

 UNION

SELECT
  aa1.analysis_name                AS "attributeName",
  cast(ar1.count_value AS VARCHAR) AS "attributeValue"
FROM @results_database_schema.ACHILLES_analysis aa1
INNER JOIN
@results_database_schema.ACHILLES_results ar1
ON aa1.analysis_id = ar1.analysis_id
WHERE aa1.analysis_id = 1
)
ORDER BY attribute_name DESC
