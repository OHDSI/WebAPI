CREATE TABLE ${ohdsiSchema}.analysis_execution (
  id              INTEGER NOT NULL ,
  analysis_id     INTEGER NOT NULL,
  analysis_type   VARCHAR2(255) NOT NULL,
  duration        INTEGER NOT NULL,
  executed        TIMESTAMP(3),
  sec_user_id     INTEGER,
  executionStatus VARCHAR2(255),
  update_password VARCHAR2(255),
  source_id       INTEGER,
  CONSTRAINT PK_analysis_execution_id PRIMARY KEY(id)
);

CREATE SEQUENCE ${ohdsiSchema}.analysis_execution_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE ${ohdsiSchema}.input_files
(
  id               NUMBER(19) NOT NULL,
  cca_execution_id NUMBER(19),
  execution_id     INTEGER,
  file_contents    CLOB,
  file_name        VARCHAR2(255) NOT NULL,
  CONSTRAINT input_files_pkey PRIMARY KEY(id)
);

CREATE TABLE ${ohdsiSchema}.output_files
(
  id               NUMBER(19) NOT NULL,
  cca_execution_id NUMBER(19),
  execution_id     INTEGER NOT NULL,
  file_contents    CLOB,
  file_name        VARCHAR2(255) NOT NULL,
  CONSTRAINT output_files_pkey PRIMARY KEY(id)
);