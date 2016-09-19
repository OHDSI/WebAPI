SELECT [preference_score] score, sum(treatment) treatment, sum(comparator) comparator
FROM @resultsTableQualifier.[cca_psmodel_scores]
WHERE execution_id = @executionId
GROUP BY [preference_score]
ORDER BY [preference_score] asc
