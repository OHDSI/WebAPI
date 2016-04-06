SELECT 
    evidence_type,
    supports,
    statistic_value
FROM @tableQualifier.penelope_laertes_universe
WHERE condition_concept_id = @conditionID AND ingredient_concept_id = @drugID 
AND evidence_type like '%@evidenceGroup%'
AND supports = 't'
