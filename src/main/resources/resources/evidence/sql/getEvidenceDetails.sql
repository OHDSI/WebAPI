SELECT evidence_linkouts
FROM @tableQualifier.penelope_laertes_universe
WHERE condition_concept_id = @conditionID 
AND ingredient_concept_id = @drugID 
AND evidence_type = '@evidenceType'
AND supports = 1
