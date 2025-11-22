-- PostgreSQL-specific concept search using pg_trgm trigram similarity
-- Feature: 001-concept-search-optimization
-- Optimizations:
--   - Uses similarity() function for fuzzy matching
--   - Leverages GIN trigram indexes (idx_concept_name_trgm, idx_concept_code_trgm)
--   - Relevance-based ordering (similarity score DESC)
--   - Configurable similarity threshold (@similarity_threshold parameter)

select CONCEPT_ID, CONCEPT_NAME, COALESCE(STANDARD_CONCEPT,'N') STANDARD_CONCEPT, COALESCE(INVALID_REASON,'V') INVALID_REASON, CONCEPT_CODE, CONCEPT_CLASS_ID, DOMAIN_ID, VOCABULARY_ID, VALID_START_DATE, VALID_END_DATE,
  @similarity_expression AS similarity_score
from @CDM_schema.concept
where 1=1@filters
order by similarity_score DESC, CONCEPT_NAME ASC
