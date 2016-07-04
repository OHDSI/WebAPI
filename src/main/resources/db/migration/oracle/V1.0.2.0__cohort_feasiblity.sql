CREATE TABLE ${ohdsiSchema}.cohort_inclusion(
  cohort_definition_id number(10) NOT NULL,
  rule_sequence number(10) NOT NULL,
  name varchar2(255) NULL,
  description varchar(1000) NULL
)
;

CREATE TABLE ${ohdsiSchema}.cohort_inclusion_result(
  cohort_definition_id number(10) NOT NULL,
  inclusion_rule_mask number(19) NOT NULL,
  person_count number(19) NOT NULL
)
;

CREATE TABLE ${ohdsiSchema}.cohort_inclusion_stats(
  cohort_definition_id number(10) NOT NULL,
  rule_sequence number(10) NOT NULL,
  person_count number(19) NOT NULL,
  gain_count number(19) NOT NULL,
  person_total number(19) NOT NULL
)
;

CREATE TABLE ${ohdsiSchema}.cohort_summary_stats(
  cohort_definition_id number(10) NOT NULL,
  base_count number(19) NOT NULL,
  final_count number(19) NOT NULL
)
;
