select distinct cr.concept_id_2 as concept_id
FROM
(
  @conceptsetQuery
) C
join @CDM_schema.concept_relationship cr on C.concept_id = cr.concept_id_1 and cr.relationship_id = 'Mapped from'
