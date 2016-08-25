SELECT [coefficient],[id],[covariate_name] 
FROM @resultsTableQualifier.[cca_ps_model]
WHERE execution_id = @executionId
