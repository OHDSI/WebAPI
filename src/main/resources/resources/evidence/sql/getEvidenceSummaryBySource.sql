SELECT 
    evidence_type,
    modality,
    statistic_value
FROM @tableQualifier.penelope_laertes_universe
WHERE condition_concept_id = @conditionID AND ingredient_concept_id = @drugID 
AND evidence_type like '%@evidenceGroup%'
AND modality = 1