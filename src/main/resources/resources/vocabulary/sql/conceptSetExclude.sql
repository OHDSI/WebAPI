LEFT JOIN
(
  @excludeQuery
) E ON I.concept_id = E.concept_id
WHERE E.concept_id is null
