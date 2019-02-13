LEFT JOIN
(
  select concept_id from @cdm_database_schema.concept where concept_id in (@conceptIds)and invalid_reason is null
  @descendantQuery
) E ON I.concept_id = E.concept_id
WHERE E.concept_id is null
