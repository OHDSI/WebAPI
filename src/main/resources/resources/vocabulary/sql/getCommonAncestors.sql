select CONCEPT_ID, CONCEPT_NAME, ISNULL(STANDARD_CONCEPT,'N') STANDARD_CONCEPT, ISNULL(INVALID_REASON,'V') INVALID_REASON, CONCEPT_CODE, CONCEPT_CLASS_ID, DOMAIN_ID, VOCABULARY_ID, 'Ancestor' RELATIONSHIP_NAME, RELATIONSHIP_DISTANCE
from @CDM_schema.concept c
join (
	select ancestor_concept_id, min(min_levels_of_separation) relationship_distance
	from @CDM_schema.concept_ancestor
	where descendant_concept_id in (@conceptIdentifierList)
	group by ancestor_concept_id
	having count(ancestor_concept_id) = @conceptIdentifierListLength
) ca on c.concept_id = ca.ancestor_concept_id
order by ca.relationship_distance