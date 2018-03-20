IF OBJECT_ID('@results_schema.cohort_inclusion', 'U') IS NULL
CREATE TABLE @results_schema.cohort_inclusion(
  cohort_definition_id int NOT NULL,
  rule_sequence int NOT NULL,
  name varchar(255) NULL,
  description varchar(1000) NULL
);

IF OBJECT_ID('@results_schema.cohort_inclusion_result', 'U') IS NULL
CREATE TABLE @results_schema.cohort_inclusion_result(
  cohort_definition_id int NOT NULL,
  inclusion_rule_mask bigint NOT NULL,
  person_count bigint NOT NULL
);

IF OBJECT_ID('@results_schema.cohort_inclusion_stats', 'U') IS NULL
CREATE TABLE @results_schema.cohort_inclusion_stats(
  cohort_definition_id int NOT NULL,
  rule_sequence int NOT NULL,
  person_count bigint NOT NULL,
  gain_count bigint NOT NULL,
  person_total bigint NOT NULL
);

IF OBJECT_ID('@results_schema.cohort_summary_stats', 'U') IS NULL
CREATE TABLE @results_schema.cohort_summary_stats(
  cohort_definition_id int NOT NULL,
  base_count bigint NOT NULL,
  final_count bigint NOT NULL
);

IF OBJECT_ID('@results_schema.feas_study_result', 'U') IS NULL
CREATE TABLE @results_schema.feas_study_result(
  study_id int NOT NULL,
  inclusion_rule_mask bigint NOT NULL,
  person_count bigint NOT NULL
);

IF OBJECT_ID('@results_schema.feas_study_inclusion_stats', 'U') IS NULL
CREATE TABLE @results_schema.feas_study_inclusion_stats(
  study_id int NOT NULL,
  rule_sequence int NOT NULL,
  name varchar(255) NOT NULL,
  person_count bigint NOT NULL,
  gain_count bigint NOT NULL,
  person_total bigint NOT NULL
);

IF OBJECT_ID('@results_schema.feas_study_index_stats', 'U') IS NULL
CREATE TABLE @results_schema.feas_study_index_stats(
  study_id int NOT NULL,
  person_count bigint NOT NULL,
  match_count bigint NOT NULL
);