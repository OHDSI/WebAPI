IF OBJECT_ID('@results_schema.feas_study_inclusion_stats', 'U') IS NULL
CREATE TABLE @results_schema.feas_study_inclusion_stats(
  study_id int NOT NULL,
  rule_sequence int NOT NULL,
  name varchar(255) NOT NULL,
  person_count bigint NOT NULL,
  gain_count bigint NOT NULL,
  person_total bigint NOT NULL
);
