  UNION 

  select c.CONCEPT_ID
  from @CDM_schema.CONCEPT c
  join @CDM_schema.CONCEPT_ANCESTOR ca on c.CONCEPT_ID = ca.DESCENDANT_CONCEPT_ID
  and ca.ANCESTOR_CONCEPT_ID in (@conceptIds)
  and c.INVALID_REASON is null
