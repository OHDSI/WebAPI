  UNION 

  select c.concept_id
  from @cdm_database_schema.concept c
  join @cdm_database_schema.concept_ancestor ca on c.concept_id = ca.descendant_concept_id
  and ca.ancestor_concept_id in (@conceptIds)
  and c.invalid_reason is null
