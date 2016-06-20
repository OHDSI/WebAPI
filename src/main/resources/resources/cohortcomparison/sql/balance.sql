SELECT [covariate_name], [concept_id], [covariate_id], [before_matching_std_diff], [after_matching_std_diff]
FROM @resultsTableQualifier.[cca_balance] 
WHERE execution_id = @executionId