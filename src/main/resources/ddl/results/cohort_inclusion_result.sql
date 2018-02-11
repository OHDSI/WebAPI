IF OBJECT_ID('@results_schema.cohort_inclusion_result', 'U') IS NULL
CREATE TABLE @results_schema.cohort_inclusion_result(
  cohort_definition_id int NOT NULL,
  inclusion_rule_mask bigint NOT NULL,
  person_count bigint NOT NULL
);
