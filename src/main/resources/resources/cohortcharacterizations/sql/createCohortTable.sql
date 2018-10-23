create table @temp_database_schema.@target_table
(
  COHORT_DEFINITION_ID int NOT NULL,
  SUBJECT_ID bigint NOT NULL,
  cohort_start_date date NOT NULL,
  cohort_end_date date NOT NULL
);

IF OBJECT_ID('@results_schema.cohort_inclusion', 'U') IS NULL
CREATE TABLE @temp_database_schema.cohort_inclusion(
  cohort_definition_id int NOT NULL,
  rule_sequence int NOT NULL,
  name varchar(255) NULL,
  description varchar(1000) NULL
);
