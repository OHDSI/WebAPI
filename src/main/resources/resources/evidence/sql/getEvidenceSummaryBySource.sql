SELECT evidence_type, count(*) as ct
FROM @tableQualifier.penelope_laertes_universe
WHERE condition_concept_id = @conditionID AND ingredient_concept_id = @drugID 
GROUP BY evidence_type