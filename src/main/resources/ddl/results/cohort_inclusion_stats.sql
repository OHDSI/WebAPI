IF OBJECT_ID('@results_schema.cohort_inclusion_stats', 'U') IS NULL
CREATE TABLE @results_schema.cohort_inclusion_stats(
  cohort_definition_id int NOT NULL,
  rule_sequence int NOT NULL,
  person_count bigint NOT NULL,
  gain_count bigint NOT NULL,
  person_total bigint NOT NULL
);
