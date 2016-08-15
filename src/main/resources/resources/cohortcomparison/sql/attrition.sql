SELECT [description], [treated_persons], [comparator_persons], [treated_exposures], [comparator_exposures] , [attrition_order]
FROM @resultsTableQualifier.[cca_attrition] 
WHERE execution_id = @executionId
ORDER BY [attrition_order] ASC
