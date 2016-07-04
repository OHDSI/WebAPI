SELECT 
    condition_concept_id
    ,condition_concept_name
    ,ingredient_concept_id
    ,ingredient_concept_name
    ,evidence_type
    ,supports
    ,statistic_value
    ,evidence_linkouts
FROM @tableQualifier.penelope_laertes_universe
WHERE condition_concept_id IN (@conditionConceptList) 
AND ingredient_concept_id IN (@ingredientConceptList)
AND evidence_type IN (@evidenceTypeList)
