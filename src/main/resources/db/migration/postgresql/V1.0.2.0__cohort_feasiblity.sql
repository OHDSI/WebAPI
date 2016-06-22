CREATE TABLE ${ohdsiSchema}.cohort_inclusion(
  cohort_definition_id int NOT NULL,
  rule_sequence int NOT NULL,
  name varchar(255) NULL,
  description varchar(1000) NULL
)
;

CREATE TABLE ${ohdsiSchema}.cohort_inclusion_result(
  cohort_definition_id int NOT NULL,
  inclusion_rule_mask bigint NOT NULL,
  person_count bigint NOT NULL
)
;

CREATE TABLE ${ohdsiSchema}.cohort_inclusion_stats(
  cohort_definition_id int NOT NULL,
  rule_sequence int NOT NULL,
  person_count bigint NOT NULL,
  gain_count bigint NOT NULL,
  person_total bigint NOT NULL
)
;

CREATE TABLE ${ohdsiSchema}.cohort_summary_stats(
  cohort_definition_id int NOT NULL,
  base_count bigint NOT NULL,
  final_count bigint NOT NULL
)
;
