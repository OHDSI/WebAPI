select distinct I.concept_id FROM
( 
  select DISTINCT concept_id from @CDM_schema.CONCEPT where concept_id in (@conceptIds) and invalid_reason is null
  @descendantQuery
) I
