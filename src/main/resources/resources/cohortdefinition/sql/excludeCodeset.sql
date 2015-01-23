LEFT JOIN
(
  select CONCEPT_ID from @CDM_schema.CONCEPT where CONCEPT_ID in (@conceptIds)and INVALID_REASON is null
  @descendantQuery
) E ON I.CONCEPT_ID = E.CONCEPT_ID
WHERE E.CONCEPT_ID is null
