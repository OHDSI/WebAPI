SELECT     
    DrugUniverse.ingredient_concept_id
    ,DrugUniverse.ingredient_concept_name
    ,CASE WHEN(LabelEvidence.ingredient_concept_id IS NULL) THEN false ELSE true END "Has_Evidence"
FROM 
(
    SELECT DISTINCT ingredient_concept_id, ingredient_concept_name
    FROM @tableQualifier.penelope_laertes_universe
    WHERE ingredient_concept_id IN (@ingredientConceptList)
) DrugUniverse
LEFT JOIN 
(
    SELECT DISTINCT
        ingredient_concept_id
        ,ingredient_concept_name
    FROM @tableQualifier.penelope_laertes_universe
    WHERE condition_concept_id IN (@conditionConceptList) 
    AND ingredient_concept_id IN (@ingredientConceptList)
    AND evidence_type IN (@evidenceTypeList)
) LabelEvidence ON DrugUniverse.ingredient_concept_id = LabelEvidence.ingredient_concept_id

