SELECT     
    DrugUniverse.ingredient_concept_id
    ,DrugUniverse.ingredient_concept_name
    ,CASE WHEN(LabelEvidence.ingredient_concept_id IS NULL) THEN 0 ELSE 1 END "Has_Evidence"
FROM 
(
    SELECT DISTINCT ingredient_concept_id, ingredient_concept_name
    FROM evidence_schema.penelope_laertes_universe
    WHERE ingredient_concept_id IN (?,?,?)
) DrugUniverse
LEFT JOIN 
(
    SELECT DISTINCT
        ingredient_concept_id
        ,ingredient_concept_name
    FROM evidence_schema.penelope_laertes_universe
    WHERE condition_concept_id IN (?,?,?) 
    AND ingredient_concept_id IN (?,?,?)
    AND evidence_type IN (?,?,?)
) LabelEvidence ON DrugUniverse.ingredient_concept_id = LabelEvidence.ingredient_concept_id

