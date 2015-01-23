select @codesetId as CODESET_ID, I.CONCEPT_ID FROM
( 
  select DISTINCT CONCEPT_ID from @CDM_schema.CONCEPT where CONCEPT_ID in (@conceptIds) and INVALID_REASON is null
  @descendantQuery
) I 
