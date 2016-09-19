CREATE TABLE ${ohdsiSchema}.ir_analysis(
  id number(10) NOT NULL,
  name varchar2(255) NOT NULL,
  description varchar2(1000) NULL,
  created_by varchar2(255) NULL,
  created_date Timestamp(3) NULL,
  modified_by varchar2(255) NULL,
  modified_date Timestamp(3) NULL,
  CONSTRAINT PK_ir_analysis PRIMARY KEY (id)
)
;

CREATE TABLE ${ohdsiSchema}.ir_analysis_details(
  id int,
  expression CLOB NOT NULL,
  CONSTRAINT PK_irad PRIMARY KEY (id),
  CONSTRAINT FK_irad_ira 
    FOREIGN KEY (id) REFERENCES ${ohdsiSchema}.ir_analysis(id)
)
;

CREATE TABLE ${ohdsiSchema}.ir_execution (
  analysis_id         number(10)      NOT NULL,
  source_id           number(10)      NOT NULL,
  start_time          Timestamp(3) NULL,
  execution_duration  number(10)      NULL,
  status              number(10)      NOT NULL,
  is_valid            number(1)      NOT NULL,
  message             varchar2(2000) NULL,
  CONSTRAINT PK_ir_exec PRIMARY KEY (analysis_id, source_id)
)
;

CREATE TABLE ${ohdsiSchema}.ir_strata(
  analysis_id number(10) NOT NULL,
  strata_sequence number(10) NOT NULL,
  name varchar2(255) NULL,
  description varchar2(1000) NULL
)
;

CREATE TABLE ${ohdsiSchema}.ir_analysis_result(
  analysis_id number(10) NOT NULL,
  target_id number(10) NOT NULL,
  outcome_id number(10) NOT NULL,
  strata_mask number(19) NOT NULL,
  person_count number(19) NOT NULL,
  time_at_risk number(19) NOT NULL,
  cases number(19) NOT NULL
)
;

CREATE TABLE ${ohdsiSchema}.ir_analysis_strata_stats(
  analysis_id number(10) NOT NULL,
  target_id number(10) NOT NULL,
  outcome_id number(10) NOT NULL,
  strata_sequence number(10) NOT NULL,
  person_count number(19) NOT NULL,
  time_at_risk number(19) NOT NULL,
  cases number(19) NOT NULL
)
;

