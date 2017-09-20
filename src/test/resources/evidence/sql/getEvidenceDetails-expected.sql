SELECT evidence_linkouts
FROM evidence_schema.penelope_laertes_universe
WHERE condition_concept_id = ? 
AND ingredient_concept_id = ? 
AND evidence_type = ?
AND supports = 1
