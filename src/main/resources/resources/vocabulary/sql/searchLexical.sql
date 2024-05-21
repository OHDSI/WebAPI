select top 100 CONCEPT_ID, CONCEPT_NAME, ISNULL(STANDARD_CONCEPT,'N') STANDARD_CONCEPT, ISNULL(INVALID_REASON,'V') INVALID_REASON, CONCEPT_CODE, CONCEPT_CLASS_ID, DOMAIN_ID, VOCABULARY_ID, VALID_START_DATE, VALID_END_DATE,
LEN(REPLACE(REPLACE(CONCEPT_NAME, ' ',''), '-', '')) as c_length,
LEN(REPLACE(REPLACE(@replace_expression,' ',''),'-','')) as r_length,
1.0 - (1.0 * LEN(REPLACE(REPLACE(@replace_expression,' ',''),'-','')) / LEN(REPLACE(REPLACE(CONCEPT_NAME, ' ',''), '-', ''))) as ratio_score
from (
  select c1.concept_id as matched_concept
  from @CDM_schema.concept c1
  where @name_filters
  union
  select cs1.concept_id as matched_concept
  from @CDM_schema.concept_synonym cs1
  where @synonym_filters
) t1
inner join @CDM_schema.concept c1 on t1.matched_concept = c1.concept_id
WHERE c1.standard_concept = 'S' @filters
order by ratio_score desc;
