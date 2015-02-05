  UNION 

  select c.concept_id
  from @CDM_schema.CONCEPT c
  join @CDM_schema.CONCEPT_ANCESTOR ca on c.concept_id = ca.descendant_concept_id
  and ca.ancestor_concept_id in (@conceptIds)
  and c.invalid_reason is null
