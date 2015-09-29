select distinct cr.concept_id_1 as concept_id
FROM
(
  @conceptsetQuery
) C
join @cdm_database_schema.concept_relationship cr on C.concept_id = cr.concept_id_2 and cr.relationship_id = 'Maps to' and cr.invalid_reason IS NULL
