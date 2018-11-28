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

IF OBJECT_ID('@temp_database_schema.cohort_inclusion_result', 'U') IS NULL
CREATE TABLE @temp_database_schema.cohort_inclusion_result(
  cohort_definition_id int NOT NULL,
  mode_id int NOT NULL DEFAULT 0,
  inclusion_rule_mask bigint NOT NULL,
  person_count bigint NOT NULL
);

IF OBJECT_ID('@temp_database_schema.cohort_inclusion_stats', 'U') IS NULL
CREATE TABLE @temp_database_schema.cohort_inclusion_stats(
  cohort_definition_id int NOT NULL,
  rule_sequence int NOT NULL,
  mode_id int NOT NULL DEFAULT 0,
  person_count bigint NOT NULL,
  gain_count bigint NOT NULL,
  person_total bigint NOT NULL
);

IF OBJECT_ID('@temp_database_schema.cohort_summary_stats', 'U') IS NULL
CREATE TABLE @temp_database_schema.cohort_summary_stats(
  cohort_definition_id int NOT NULL,
  mode_id int NOT NULL DEFAULT 0,
  base_count bigint NOT NULL,
  final_count bigint NOT NULL
);
