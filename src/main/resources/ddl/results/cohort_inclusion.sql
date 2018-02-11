IF OBJECT_ID('@results_schema.cohort_inclusion', 'U') IS NULL
CREATE TABLE @results_schema.cohort_inclusion(
  cohort_definition_id int NOT NULL,
  rule_sequence int NOT NULL,
  name varchar(255) NULL,
  description varchar(1000) NULL
);
