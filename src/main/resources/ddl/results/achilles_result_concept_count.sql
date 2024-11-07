IF OBJECT_ID('@results_schema.achilles_result_concept_count', 'U') IS NULL
CREATE TABLE @results_schema.achilles_result_concept_count (
  concept_id                int,
  record_count              bigint,
  descendant_record_count   bigint,
  person_count              bigint,
  descendant_person_count   bigint
);