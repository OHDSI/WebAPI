SELECT DISTINCT 
    c2.concept_id, 
    c2.concept_name, 
    ISNULL(c2.STANDARD_CONCEPT,'N') STANDARD_CONCEPT, 
    ISNULL(c2.INVALID_REASON,'V') INVALID_REASON, 
    c2.CONCEPT_CODE, 
    c2.concept_class_id, 
    c2.domain_id, 
    c2.vocabulary_id, 
    'Is descendent of ancestor of' relationship_name, 
    ca1.min_levels_of_separation + ca2.min_levels_of_separation relationship_distance
FROM vocab_schema.concept_ancestor ca1
INNER JOIN vocab_schema.concept c1
ON ca1.ancestor_concept_id = c1.concept_id
INNER JOIN vocab_schema.concept_ancestor ca2
ON ca1.ancestor_concept_id = ca2.ancestor_concept_id
INNER JOIN vocab_schema.concept c2
ON ca2.descendant_concept_id = c2.concept_id
WHERE ca1.descendant_concept_id = ?
AND c1.vocabulary_id = ?
AND c1.concept_class_id = ?
AND c2.vocabulary_id = ?
AND c2.concept_class_id = ?
