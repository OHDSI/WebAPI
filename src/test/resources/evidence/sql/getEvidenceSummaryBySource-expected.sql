SELECT 
    evidence_type,
    supports,
    statistic_value
FROM evidence_schema.penelope_laertes_universe
WHERE condition_concept_id = ? AND ingredient_concept_id = ? 
AND evidence_type like ?
AND supports = 't'
