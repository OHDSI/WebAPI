select CONCEPT_ID, CONCEPT_NAME, ISNULL(STANDARD_CONCEPT,'N') STANDARD_CONCEPT, ISNULL(c.INVALID_REASON,'V') INVALID_REASON, CONCEPT_CODE, CONCEPT_CLASS_ID, DOMAIN_ID, VOCABULARY_ID
from @CDM_schema.concept_relationship cr
join @CDM_schema.concept c on c.concept_id = cr.concept_id_1
where cr.concept_id_2 in (@identifiers)
and relationship_id in ('Maps to')
and standard_concept IS NULL
order by domain_id, vocabulary_id
