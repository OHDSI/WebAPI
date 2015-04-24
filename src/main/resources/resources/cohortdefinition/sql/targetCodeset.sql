select @codesetId as codeset_id, I.concept_id FROM
( 
  select DISTINCT concept_id from @cdm_database_schema.CONCEPT where concept_id in (@conceptIds) and invalid_reason is null
  @descendantQuery
) I 
