SELECT 
    condition_concept_id
    ,condition_concept_name
    ,ingredient_concept_id
    ,ingredient_concept_name
    ,evidence_type
    ,supports
    ,statistic_value
    ,evidence_linkouts
FROM evidence_schema.penelope_laertes_universe
WHERE condition_concept_id IN (?,?) 
AND ingredient_concept_id IN (?,?)
AND evidence_type IN (?,?)
