SELECT [coefficient],[id],[covariate_name] 
FROM @resultsTableQualifier.[cca_psmodel]
WHERE execution_id = @executionId
