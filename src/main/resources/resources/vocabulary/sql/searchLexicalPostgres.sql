-- PostgreSQL-specific lexical concept search using pg_trgm trigram similarity
-- Feature: 001-concept-search-optimization
-- Optimizations:
--   - Uses similarity() function for fuzzy synonym matching
--   - Leverages GIN trigram indexes (idx_concept_name_trgm, idx_concept_synonym_name_trgm)
--   - Relevance-based ordering (similarity score DESC)
--   - Configurable similarity threshold (@similarity_threshold parameter)

select CONCEPT_ID, CONCEPT_NAME, COALESCE(STANDARD_CONCEPT,'N') STANDARD_CONCEPT, COALESCE(INVALID_REASON,'V') INVALID_REASON, CONCEPT_CODE, CONCEPT_CLASS_ID, DOMAIN_ID, VOCABULARY_ID, VALID_START_DATE, VALID_END_DATE,
  @similarity_expression AS similarity_score
from (
  select c1.concept_id as matched_concept,
    @name_similarity_expression as name_sim
  from @CDM_schema.concept c1
  where @name_filters
  union
  select cs1.concept_id as matched_concept,
    @synonym_similarity_expression as name_sim
  from @CDM_schema.concept_synonym cs1
  where @synonym_filters
) t1
inner join @CDM_schema.concept c1 on t1.matched_concept = c1.concept_id
WHERE c1.standard_concept = 'S' @filters
order by similarity_score DESC, CONCEPT_NAME ASC
limit 100;
