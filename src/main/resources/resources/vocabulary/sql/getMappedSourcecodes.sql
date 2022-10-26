-- get all of the concepts that were part of the list of concepts provided

Select concept_id, concept_name, ISNULL(standard_concept, 'N') standard_concept, ISNULL(invalid_reason, 'V') INVALID_REASON,
CONCEPT_CODE, CONCEPT_CLASS_ID, DOMAIN_ID, VOCABULARY_ID, VALID_START_DATE, VALID_END_DATE
from @CDM_schema.concept
where concept_id in (@identifiers)

UNION

--get all source codes that map to the list of concepts provided

select CONCEPT_ID, CONCEPT_NAME, ISNULL(STANDARD_CONCEPT,'N') STANDARD_CONCEPT, ISNULL(c.INVALID_REASON,'V') INVALID_REASON, CONCEPT_CODE, CONCEPT_CLASS_ID, DOMAIN_ID, VOCABULARY_ID, c.VALID_START_DATE, c.VALID_END_DATE
from @CDM_schema.concept_relationship cr
join @CDM_schema.concept c on c.concept_id = cr.concept_id_1
where cr.concept_id_2 in (@identifiers)
and cr.INVALID_REASON is null
and relationship_id in ('Maps to')

UNION

--get anything that may be stashed in the source-to-concept-map table

select 0 as CONCEPT_ID, SOURCE_CODE_DESCRIPTION as CONCEPT_NAME, 'N' as STANDARD_CONCEPT, ISNULL(INVALID_REASON,'V') INVALID_REASON,
SOURCE_CODE as CONCEPT_CODE, NULL as CONCEPT_CLASS_ID, NULL as DOMAIN_ID, SOURCE_VOCABULARY_ID as VOCABULARY_ID, VALID_START_DATE, VALID_END_DATE
from @CDM_schema.SOURCE_TO_CONCEPT_MAP
where TARGET_CONCEPT_ID in (@identifiers)
and INVALID_REASON is null
order by domain_id, vocabulary_id
