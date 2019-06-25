select CONCEPT_ID, CONCEPT_NAME, ISNULL(STANDARD_CONCEPT,'N') STANDARD_CONCEPT, ISNULL(INVALID_REASON,'V') INVALID_REASON, CONCEPT_CODE, CONCEPT_CLASS_ID, DOMAIN_ID, VOCABULARY_ID, 'Descendant' RELATIONSHIP_NAME, MIN_LEVELS_OF_SEPARATION RELATIONSHIP_DISTANCE
from @CDM_schema.concept_ancestor ca
join @CDM_schema.concept c on c.concept_id = ca.descendant_concept_id
where ancestor_concept_id = @id
