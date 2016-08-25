SELECT [ps],[treatment],[comparator]
FROM @resultsTableQualifier.[cca_ps_model_agg]
WHERE execution_id = @executionId