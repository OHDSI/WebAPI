select ca.ANCESTOR_CONCEPT_ID, c2.concept_name, c.CONCEPT_ID, c.CONCEPT_NAME, ISNULL(c.STANDARD_CONCEPT,'N') STANDARD_CONCEPT, ISNULL(c.INVALID_REASON,'V') INVALID_REASON, c.CONCEPT_CODE, c.CONCEPT_CLASS_ID, c.DOMAIN_ID, c.VOCABULARY_ID, 'Descendant' RELATIONSHIP_NAME, ca.MIN_LEVELS_OF_SEPARATION RELATIONSHIP_DISTANCE
from vocab_schema.CONCEPT_ANCESTOR ca
join vocab_schema.CONCEPT c on c.concept_id = ca.descendant_concept_id
join vocab_schema.CONCEPT c2 ON ca.ancestor_concept_id = c2.concept_id
where ca.ancestor_concept_id IN (?,?,?,?,?)
