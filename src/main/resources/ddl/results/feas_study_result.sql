IF OBJECT_ID('@results_schema.feas_study_result', 'U') IS NULL
CREATE TABLE $results_schema.feas_study_result(
  study_id int NOT NULL,
  inclusion_rule_mask bigint NOT NULL,
  person_count bigint NOT NULL
);
