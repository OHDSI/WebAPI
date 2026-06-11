SELECT CONCEPT_ID, CONCEPT_NAME, ISNULL(STANDARD_CONCEPT,'N') STANDARD_CONCEPT,
       ISNULL(INVALID_REASON,'V') INVALID_REASON, CONCEPT_CODE, CONCEPT_CLASS_ID,
       DOMAIN_ID, VOCABULARY_ID, VALID_START_DATE, VALID_END_DATE,
       'Ancestor' RELATIONSHIP_NAME, RELATIONSHIP_DISTANCE
FROM @vocabulary_database_schema.concept c
JOIN (
  SELECT ancestor_concept_id, MIN(min_levels_of_separation) relationship_distance
  FROM @vocabulary_database_schema.concept_ancestor
  WHERE descendant_concept_id IN (@conceptIdentifierList)
  GROUP BY ancestor_concept_id
  HAVING COUNT(ancestor_concept_id) = @conceptIdentifierListLength
) ca ON c.concept_id = ca.ancestor_concept_id
ORDER BY ca.relationship_distance
