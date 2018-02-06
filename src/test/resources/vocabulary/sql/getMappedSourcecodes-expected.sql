Select concept_id, concept_name, ISNULL(standard_concept, 'N') standard_concept, ISNULL(invalid_reason, 'V') INVALID_REASON,
CONCEPT_CODE, CONCEPT_CLASS_ID, DOMAIN_ID, VOCABULARY_ID
from vocab_schema.concept
where concept_id in (?,?,?,?,?,?,?)

UNION

select CONCEPT_ID, CONCEPT_NAME, ISNULL(STANDARD_CONCEPT,'N') STANDARD_CONCEPT, ISNULL(c.INVALID_REASON,'V') INVALID_REASON, CONCEPT_CODE, CONCEPT_CLASS_ID, DOMAIN_ID, VOCABULARY_ID
from vocab_schema.concept_relationship cr
join vocab_schema.concept c on c.concept_id = cr.concept_id_1
where cr.concept_id_2 in (?,?,?,?,?,?,?)
and cr.INVALID_REASON is null
and relationship_id in ('Maps to')

UNION

select 0 as CONCEPT_ID, SOURCE_CODE_DESCRIPTION as CONCEPT_NAME, 'N' as STANDARD_CONCEPT, ISNULL(INVALID_REASON,'V') INVALID_REASON,
SOURCE_CODE as CONCEPT_CODE, NULL as CONCEPT_CLASS_ID, NULL as DOMAIN_ID, SOURCE_VOCABULARY_ID as VOCABULARY_ID
from vocab_schema.SOURCE_TO_CONCEPT_MAP
where TARGET_CONCEPT_ID in (?,?,?,?,?,?,?)
and INVALID_REASON is null
order by domain_id, vocabulary_id
