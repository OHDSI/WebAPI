SELECT [propensity_score] score, sum(treatment) treatment, sum(comparator) comparator
FROM @resultsTableQualifier.[cca_psmodel_scores]
WHERE execution_id = @executionId
GROUP BY [propensity_score]
ORDER BY [propensity_score] asc
