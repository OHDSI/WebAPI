create table @temp_database_schema.@target_table
(
  COHORT_DEFINITION_ID int NOT NULL,
  SUBJECT_ID bigint NOT NULL,
  cohort_start_date date NOT NULL,
  cohort_end_date date NOT NULL
);
