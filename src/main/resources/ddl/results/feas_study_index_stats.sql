IF OBJECT_ID('@results_schema.feas_study_index_stats', 'U') IS NULL
CREATE TABLE $results_schema.feas_study_index_stats(
  study_id int NOT NULL,
  person_count bigint NOT NULL,
  match_count bigint NOT NULL
);
