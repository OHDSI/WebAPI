CREATE TABLE ${ohdsiSchema}.feas_study_generation_info(
  study_id number(10) NOT NULL,
  start_time Timestamp(3) NULL,
  execution_duration number(10) NULL,
  status number(10) NOT NULL,
  is_valid number(1) NOT NULL,
  CONSTRAINT PK_fs_gen_info PRIMARY KEY (study_id)
);

CREATE TABLE ${ohdsiSchema}.feasibility_study(
  id number(10) NOT NULL,
  name varchar2(255) NOT NULL,
  description varchar2(1000) NULL,
  index_def_id number(10) NULL,
  result_def_id number(10) NULL,
  generate_info_id number(10) NULL,
  created_by varchar2(255) NULL,
  created_date Timestamp(3) NULL,
  modified_by varchar2(255) NULL,
  modified_date Timestamp(3) NULL,
  CONSTRAINT PK_feasibility_study PRIMARY KEY (id),
  CONSTRAINT FK_fs_cd_index FOREIGN KEY(index_def_id)
    REFERENCES ${ohdsiSchema}.cohort_definition (id),
  CONSTRAINT FK_fs_cd_result FOREIGN KEY(result_def_id)
    REFERENCES ${ohdsiSchema}.cohort_definition (id),
  CONSTRAINT FK_fs_fsgi FOREIGN KEY(generate_info_id)
    REFERENCES ${ohdsiSchema}.feas_study_generation_info (study_id)
);

CREATE TABLE ${ohdsiSchema}.feasibility_inclusion(
  study_id number(10) NOT NULL,
  sequence number(10) NOT NULL,
  name varchar2(255) NULL,
  description varchar2(1000) NULL,
  expression Clob NULL,
  CONSTRAINT FK_fi_fs FOREIGN KEY(study_id)
    REFERENCES ${ohdsiSchema}.feasibility_study (id)
    ON DELETE CASCADE
);

CREATE TABLE ${ohdsiSchema}.feas_study_result(
  study_id number(10) NOT NULL,
  inclusion_rule_mask number(19) NOT NULL,
  person_count number(19) NOT NULL
);

CREATE TABLE ${ohdsiSchema}.feas_study_index_stats(
  study_id number(10) NOT NULL,
  person_count number(19) NOT NULL,
  match_count number(19) NOT NULL
);


CREATE TABLE feas_study_inclusion_stats(
  study_id number(10) NOT NULL,
  rule_sequence number(10) NOT NULL,
  name varchar2(255) NOT NULL,
  person_count number(19) NOT NULL,
  gain_count number(19) NOT NULL,
  person_total number(19) NOT NULL
);